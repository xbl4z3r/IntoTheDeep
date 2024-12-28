package org.firstinspires.ftc.teamcode.teleop;

import static org.firstinspires.ftc.teamcode.Utilities.setTimeout;

import com.acmerobotics.roadrunner.Math;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Const;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.BaseOpMode;
import org.firstinspires.ftc.teamcode.InputSystem;
import org.firstinspires.ftc.teamcode.RobotHardware;
import org.firstinspires.ftc.teamcode.Utilities;

@TeleOp(name = "Demo Op", group = "TeleOp")
public final class DemoOp extends BaseOpMode
{
	private InputSystem driveInput, armInput;
	private RobotHardware robot;

	public static class Constants
	{
		public static float EXTENDO_RETRACTED = 0.38f;
		public static float EXTENDO_EXTENDED = 0f;

		public static float TUMBLER_DELIVER = 0.17f;
		public static float TUMBLER_IDLE = 0.335f;
		public static float TUMBLER_READY = 0.8f;
		public static float TUMBLER_PICKUP = 0.9f;
		public static float TUMBLER_WALL = 0.6f;
		public static float SMALL_TUMBLER_IDLE = 0f;
		public static float SMALL_TUMBLER_WALL = 0.55f;
		public static float SMALL_TUMBLER_BUSY = 1f;

		public static float ROTATOR_BASE = 0.5f;

		public static float CLAW_OPEN = 1f;
		public static float CLAW_CLOSED = 0f;

		public static int LIFT_IDLE = 0;
		public static int LIFT_SUSPEND = 900;

		// BASKET_LOW, BASKET_HIGH, CHAMBER_HIGH
		public static int[] LIFT_LEVELS = {650, 1550, 250, 0};
		public static int LIFT_OFFSET = 250; // The amount of ticks to subtract to hang the specimen on the submersible

		public static float SCORE_TUMBLER_IDLE = 0.05f;
		public static float SCORE_TUMBLER_CHAMBER = 0.85f;
		public static float SCORE_TUMBLER_BASKET = 0.92f;
		public static float SCORE_TUMBLER_DROP = 1f;

		public static float SCORE_CLAW_OPEN = 0f;
		public static float SCORE_CLAW_CLOSED = 0.5f;
	}

	private static class Keybindings
	{
		public static class Arm
		{
			public static final InputSystem.Key BASKET_LOW_KEY = new InputSystem.Key("dpad_down");
			public static final InputSystem.Key BASKET_HIGH_KEY = new InputSystem.Key("dpad_up");
			public static final InputSystem.Key CHAMBER_HIGH_KEY = new InputSystem.Key("dpad_left");
			public static final InputSystem.Key CHAMBER_ZERO_KEY = new InputSystem.Key("dpad_right");
			public static final InputSystem.Key PRIMARY_KEY = new InputSystem.Key("a");
			public static final InputSystem.Key SUSPEND_KEY = new InputSystem.Key("y");
			public static final InputSystem.Key CANCEL_SUSPEND_KEY = new InputSystem.Key("x");
			public static final InputSystem.Key DROP_KEY = new InputSystem.Key("b");
		}

		public static class Drive
		{
			public static final InputSystem.Key EXTENDO_FULL_KEY = new InputSystem.Key("dpad_down");
			public static final InputSystem.Key EXTENDO_HALF_KEY = new InputSystem.Key("dpad_left");
			public static final InputSystem.Key EXTENDO_ZERO_KEY = new InputSystem.Key("dpad_up");
			public static final InputSystem.Key EXTENDO_WALL_PICKUP = new InputSystem.Key("dpad_right");
			public static final InputSystem.Key TURBO_KEY = new InputSystem.Key("right_bumper");
			public static final InputSystem.Key SUPPRESS_KEY = new InputSystem.Key("left_bumper");
			public static final InputSystem.Axis DRIVE_X = new InputSystem.Axis("left_stick_x");
			public static final InputSystem.Axis DRIVE_Y = new InputSystem.Axis("left_stick_y");
			public static final InputSystem.Axis DRIVE_ROT_L = new InputSystem.Axis("left_trigger");
			public static final InputSystem.Axis DRIVE_ROT_R = new InputSystem.Axis("right_trigger");
			public static final InputSystem.Axis ROTATOR_ANGLE = new InputSystem.Axis("right_stick_x");
			public static final InputSystem.Key PRIMARY_KEY = new InputSystem.Key("a");
		}
	}

	@Override
	protected void OnInitialize()
	{
		driveInput = input1;
		armInput = input2;

		robot = new RobotHardware(hardwareMap);
		robot.init();
	}

	@Override
	protected void OnRun()
	{
		Suspender();
		if (suspending) return;
		Drive();
		if (suspendState == Utilities.State.BUSY) return;
		Extendo();
		Scorer();
	}

	private void Drive()
	{
		float speed = 0.7f;
		if (driveInput.isPressed(Keybindings.Drive.TURBO_KEY)) speed = 1.0f;
		else if (driveInput.isPressed(Keybindings.Drive.SUPPRESS_KEY)) speed = 0.4f;
		robot.drivetrain.setDrivePowers(
				new PoseVelocity2d(new Vector2d(driveInput.getValue(Keybindings.Drive.DRIVE_Y),
						driveInput.getValue(Keybindings.Drive.DRIVE_X)).times(-speed),
						driveInput.getValue(Keybindings.Drive.DRIVE_ROT_L) - driveInput.getValue(Keybindings.Drive.DRIVE_ROT_R)
				)
		);
	}

	private Utilities.State extendoState = Utilities.State.IDLE;
	private Utilities.State scorerState = Utilities.State.IDLE;
	private boolean loaded = false;
	private boolean rotatorLocked = true;
	private boolean wallPicking = false;

	private float rotatorValue = Constants.ROTATOR_BASE;
	private float extendoValue = Constants.EXTENDO_EXTENDED;
	private int liftLevel = 0;

	/*
	 *                                         ┌──────────────────┐
	 *                                         │scorerState: IDLE │
	 *                                         │                  │
	 *         ┌───────┐                       │extendoState: IDLE│
	 *         │STATE  │                  ┌────┤                  │
	 *         │MACHINE│                  │    │rotator: LOCKED   │
	 *         └───────┘                  │    │                  │
	 *                                    │    │loaded: FALSE     │
	 *                                    │    └──────────────────┘   ┌──────────────────┐
	 *                                    │                           │scorerState: IDLE │
	 *                                 ┌──┴─┐        A                │                  │
	 *                            ┌───►│Idle├─────────────────┐       │extendoState: BUSY│
	 *                            │    └────┘                 │       │                  │
	 *                            │       ▲                   ▼       │rotator: UNLOCKED │
	 *                            │       │               ┌────────┐  │                  │
	 *             A (Arm Gamepad)│       │               │Extended├──┤loaded: FALSE     │
	 *                            │       │               └───┬────┘  └──────────────────┘
	 *                            │       │                   │
	 * ┌──────────────────┐       │       │                   │A      ┌──────────────────┐
	 * │scorerState: BUSY │       │       │       FAIL        │       │scorerState: IDLE │
	 * │                  │       │       └───────────────────┤       │                  │
	 * │extendoState: IDLE│       │                           │SUCCESS│extendoState: IDLE│
	 * │                  │       │                           │       │                  │
	 * │rotator: LOCKED   │       │                           ▼       │rotator: LOCKED   │
	 * │                  │   ┌───┴───┐    A (Arm Gamepad) ┌──────┐   │                  │
	 * │loaded: TRUE      ├───┤Scoring│◄───────────────────┤Loaded├───┤loaded: TRUE      │
	 * └──────────────────┘   └───────┘                    └──────┘   └──────────────────┘
	 *
	 */
	private void Extendo()
	{
		if (!rotatorLocked) {
			rotatorValue += (float) (driveInput.getValue(Keybindings.Drive.ROTATOR_ANGLE) / 100f);
			rotatorValue = (float) Math.clamp(rotatorValue, 0, 1);
			robot.rotatorServo.setPosition(rotatorValue);
		}

		if (driveInput.wasPressedThisFrame(Keybindings.Drive.EXTENDO_FULL_KEY)) {
			extendoValue = Constants.EXTENDO_EXTENDED;
			robot.extendoServo.setPosition(extendoValue);
			robot.tumblerServo.setPosition(Constants.TUMBLER_READY);
			robot.smallTumbler.setPosition(Constants.SMALL_TUMBLER_BUSY);
			wallPicking = false;
		} else if (driveInput.wasPressedThisFrame(Keybindings.Drive.EXTENDO_HALF_KEY)) {
			extendoValue = (Constants.EXTENDO_RETRACTED + Constants.EXTENDO_EXTENDED) / 2f;
			robot.extendoServo.setPosition(extendoValue);
			robot.tumblerServo.setPosition(Constants.TUMBLER_READY);
			robot.smallTumbler.setPosition(Constants.SMALL_TUMBLER_BUSY);
			wallPicking = false;
		} else if (driveInput.wasPressedThisFrame(Keybindings.Drive.EXTENDO_ZERO_KEY)) {
			extendoValue = Constants.EXTENDO_RETRACTED;
			robot.extendoServo.setPosition(extendoValue);
			robot.tumblerServo.setPosition(Constants.TUMBLER_READY);
			robot.smallTumbler.setPosition(Constants.SMALL_TUMBLER_BUSY);
			wallPicking = false;
		} else if (driveInput.isPressed(Keybindings.Drive.EXTENDO_WALL_PICKUP)) {
			wallPicking = true;
			extendoValue = Constants.EXTENDO_RETRACTED;
			robot.extendoServo.setPosition(extendoValue);
			robot.tumblerServo.setPosition(Constants.TUMBLER_WALL);
			robot.smallTumbler.setPosition(Constants.SMALL_TUMBLER_WALL);
		}

		if (!driveInput.wasPressedThisFrame(Keybindings.Drive.PRIMARY_KEY)) return;

		if (scorerState == Utilities.State.BUSY)
		{
			if (liftLevel < 2) {
				robot.scoreClaw.setPosition(Constants.SCORE_CLAW_OPEN);
				setTimeout(() -> {
					robot.scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
					setTimeout(() -> {
						robot.lift1.setTargetPosition(Constants.LIFT_IDLE);
						robot.lift2.setTargetPosition(Constants.LIFT_IDLE);

						scorerState = Utilities.State.IDLE;
						loaded = false; // Once the scoring is done, the claw is no longer loaded
					}, 1000);
				}, 500);
			} else {
				robot.scoreClaw.setPosition(Constants.SCORE_CLAW_OPEN);
				setTimeout(() -> {
					robot.scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
					setTimeout(() -> {
						robot.lift1.setTargetPosition(Constants.LIFT_IDLE);
						robot.lift2.setTargetPosition(Constants.LIFT_IDLE);

						scorerState = Utilities.State.IDLE;
						loaded = false; // Once the scoring is done, the claw is no longer loaded
					}, 400);
				}, 400);
			}
			return;
		}

		if (extendoState == Utilities.State.IDLE) {
			extendoState = Utilities.State.BUSY;
			rotatorLocked = false;

			if (scorerState == Utilities.State.IDLE)
				loaded = false; // If the scorer is not busy and the extendo is busy, the claw is not loaded

			robot.extendoServo.setPosition(extendoValue);
			robot.tumblerServo.setPosition(Constants.TUMBLER_READY);
			robot.smallTumbler.setPosition(Constants.SMALL_TUMBLER_BUSY);
		} else if (extendoState == Utilities.State.BUSY) {
			rotatorLocked = true;

			robot.scoreClaw.setPosition(Constants.SCORE_CLAW_OPEN); // In case the claw is closed
			if (!wallPicking)
				robot.tumblerServo.setPosition(Constants.TUMBLER_PICKUP);
			setTimeout(() -> {
				robot.clawServo.setPosition(Constants.CLAW_CLOSED);
				setTimeout(() -> {
					robot.extendoServo.setPosition(Constants.EXTENDO_RETRACTED);
					robot.tumblerServo.setPosition(Constants.TUMBLER_READY);
					robot.smallTumbler.setPosition(Constants.SMALL_TUMBLER_IDLE);
					robot.rotatorServo.setPosition(Constants.ROTATOR_BASE);
					rotatorValue = Constants.ROTATOR_BASE;
					setTimeout(() -> {
						robot.tumblerServo.setPosition(Constants.TUMBLER_DELIVER);
						setTimeout(() -> {
							robot.scoreClaw.setPosition(Constants.SCORE_CLAW_CLOSED);
							robot.clawServo.setPosition(Constants.CLAW_OPEN);
							setTimeout(() -> {
								robot.tumblerServo.setPosition(Constants.TUMBLER_IDLE);

								extendoState = Utilities.State.IDLE;
								loaded = true; // If the extendo is idle, the claw should be loaded
							}, 500);
						}, 500);
					}, 100);
				}, 500);
			}, 400);
		}
	}

	private boolean liftSkibidy = false;
	private boolean tumblerSkibidy = false;
	private void Scorer()
	{

		if (armInput.wasPressedThisFrame(Keybindings.Arm.BASKET_LOW_KEY)) liftLevel = 0;
		else if (armInput.wasPressedThisFrame(Keybindings.Arm.BASKET_HIGH_KEY)) liftLevel = 1;
		else if (armInput.wasPressedThisFrame(Keybindings.Arm.CHAMBER_HIGH_KEY)) liftLevel = 2;
		else if (armInput.wasPressedThisFrame(Keybindings.Arm.CHAMBER_ZERO_KEY)) liftLevel = 3;

		if (scorerState == Utilities.State.BUSY && !liftSkibidy) {
			if (robot.scoreTumbler.getPosition() != Constants.SCORE_TUMBLER_IDLE && tumblerSkibidy)
				robot.scoreTumbler.setPosition(liftLevel == 2 ? Constants.SCORE_TUMBLER_CHAMBER : Constants.SCORE_TUMBLER_BASKET);

			robot.lift1.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);
			robot.lift2.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);
		} else if (scorerState == Utilities.State.IDLE) {
			robot.lift1.setTargetPosition(Constants.LIFT_IDLE);
			robot.lift2.setTargetPosition(Constants.LIFT_IDLE);
			robot.scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
		}

		if (!loaded
			|| !armInput.wasPressedThisFrame(Keybindings.Arm.PRIMARY_KEY))
			return;
		if (scorerState == Utilities.State.IDLE) {
			scorerState = Utilities.State.BUSY;

			robot.lift1.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);
			robot.lift2.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);

			robot.scoreClaw.setPosition(Constants.SCORE_CLAW_CLOSED);
			tumblerSkibidy = true;
			setTimeout(() -> robot.scoreTumbler.setPosition(liftLevel == 2 ? Constants.SCORE_TUMBLER_CHAMBER : Constants.SCORE_TUMBLER_BASKET), 400);
		} else if (scorerState == Utilities.State.BUSY) {
			if (liftLevel < 2) {
				robot.scoreClaw.setPosition(Constants.SCORE_CLAW_OPEN);
				setTimeout(() -> {
					tumblerSkibidy = false;
					robot.scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
					setTimeout(() -> {
						robot.lift1.setTargetPosition(Constants.LIFT_IDLE);
						robot.lift2.setTargetPosition(Constants.LIFT_IDLE);

						scorerState = Utilities.State.IDLE;
						loaded = false; // Once the scoring is done, the claw is no longer loaded
					}, 1000);
				}, 500);

			} else {
				liftSkibidy = true;
				robot.lift1.setTargetPosition(Constants.LIFT_LEVELS[liftLevel] - Constants.LIFT_OFFSET);
				robot.lift2.setTargetPosition(Constants.LIFT_LEVELS[liftLevel] - Constants.LIFT_OFFSET);
				setTimeout(() -> {
					robot.scoreClaw.setPosition(Constants.SCORE_CLAW_OPEN);
					setTimeout(() -> {
						tumblerSkibidy = false;
						robot.scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
						setTimeout(() -> {
							robot.lift1.setTargetPosition(Constants.LIFT_IDLE);
							robot.lift2.setTargetPosition(Constants.LIFT_IDLE);

							scorerState = Utilities.State.IDLE;
							setTimeout(() -> liftSkibidy = false, 10);

							loaded = false; // Once the scoring is done, the claw is no longer loaded
						}, 400);
					}, 400);
				}, 500);
			}
		}
	}


	private Utilities.State suspendState = Utilities.State.IDLE;
	private boolean suspending = false;

	private void Suspender()
	{
		if (armInput.wasPressedThisFrame(Keybindings.Arm.CANCEL_SUSPEND_KEY) && suspendState == Utilities.State.BUSY) {
			robot.lift1.setTargetPosition(Constants.LIFT_IDLE);
			robot.lift2.setTargetPosition(Constants.LIFT_IDLE);
			suspendState = Utilities.State.IDLE;
			return;
		}

		if (!armInput.wasPressedThisFrame(Keybindings.Arm.SUSPEND_KEY)) return;

		if (suspendState == Utilities.State.IDLE) {
			robot.lift1.setTargetPosition(Constants.LIFT_SUSPEND);
			robot.lift2.setTargetPosition(Constants.LIFT_SUSPEND);
			suspendState = Utilities.State.BUSY;
		} else {
			suspending = true;
			robot.lift1.setTargetPosition(Constants.LIFT_IDLE);
			robot.lift2.setTargetPosition(Constants.LIFT_IDLE);

			robot.drivetrain.setDrivePowers(new PoseVelocity2d(new Vector2d(-0.4, 0), 0));

			setTimeout(() -> robot.drivetrain.setDrivePowers(new PoseVelocity2d(new Vector2d(0, 0), 0)), 5000);
		}
	}

	@Override
	protected void OnTelemetry(Telemetry telemetry)
	{
		telemetry.addData("lift1 pos", robot.lift1.getCurrentPosition());
		telemetry.addData("lift2 pos", robot.lift2.getCurrentPosition());

		if (suspendState == Utilities.State.BUSY) {
			telemetry.addLine("[INFO] In Suspend Mode");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.SUSPEND_KEY + " again to suspend");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.CANCEL_SUSPEND_KEY + " to cancel");
		} else {
			telemetry.addLine("[INFO] Welcome Drivers!");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.SUSPEND_KEY + " to enter Suspend Mode");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.BASKET_LOW_KEY + " to move to Basket Low");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.BASKET_HIGH_KEY + " to move to Basket High");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.CHAMBER_HIGH_KEY + " to move to Submersible High");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.PRIMARY_KEY + " to score");
			telemetry.addLine("[INFO] Press " + Keybindings.Drive.PRIMARY_KEY + " to grab");
		}
	}
}