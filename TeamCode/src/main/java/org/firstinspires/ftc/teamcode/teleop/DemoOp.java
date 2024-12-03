package org.firstinspires.ftc.teamcode.teleop;

import static org.firstinspires.ftc.teamcode.Utilities.setTimeout;

import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.BaseOpMode;
import org.firstinspires.ftc.teamcode.InputSystem;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.Utilities;

@TeleOp(name = "Demo Op", group = "TeleOp")
public final class DemoOp extends BaseOpMode
{
	private InputSystem driveInput, armInput;
	private MecanumDrive mecanumDrive;
	private DcMotorEx lift1, lift2, lift3;
	private Servo extendoServo, tumblerServo, smallTumbler, rotatorServo, clawServo, scoreTumbler, scoreClaw;

	private static class Constants
	{
		public static float EXTENDO_RETRACTED = 0.25f;
		public static float EXTENDO_EXTENDED = 0.75f;

		public static float TUMBLER_DELIVER = 0.25f;
		public static float TUMBLER_IDLE = 0.35f;
		public static float TUMBLER_READY = 0.85f;
		public static float TUMBLER_PICKUP = 1f;
		public static float TUMBLER_WALL = 0.737f;
		public static float SMALL_TUMBLER_IDLE = 0f;
		public static float SMALL_TUMBLER_WALL = 0.6f;
		public static float SMALL_TUMBLER_BUSY = 1f;

		public static float ROTATOR_BASE = 0.5f;

		public static float CLAW_IDLE = 1f;
		public static float CLAW_BUSY = 0f;

		public static int LIFT_IDLE = 0;
		public static int LIFT_SUSPEND = 600;

		// BASKET_LOW, BASKET_HIGH, CHAMBER_HIGH
		public static int[] LIFT_LEVELS = {400, 1300, 140, 0};
		public static int LIFT_OFFSET = 200; // The amount of ticks to subtract to hang the specimen on the submersible

		public static float SCORE_TUMBLER_IDLE = 0.01f;
		public static float SCORE_TUMBLER_BUSY = 1f;

		public static float SCORE_CLAW_IDLE = 0f;
		public static float SCORE_CLAW_BUSY = 0.3f;
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

		mecanumDrive = new MecanumDrive(hardwareMap);

		lift1 = hardwareMap.get(DcMotorEx.class, "lift1");
		lift2 = hardwareMap.get(DcMotorEx.class, "lift2");
		lift3 = hardwareMap.get(DcMotorEx.class, "lift3");
		lift1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift3.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift3.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift1.setTargetPosition(0);
		lift2.setTargetPosition(0);
		lift3.setTargetPosition(0);
		lift1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift3.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift1.setDirection(DcMotorSimple.Direction.REVERSE);
		lift1.setPower(1);
		lift2.setPower(1);
		lift3.setPower(1);

		extendoServo = hardwareMap.get(Servo.class, "extendo");
		tumblerServo = hardwareMap.get(Servo.class, "tumbler");
		smallTumbler = hardwareMap.get(Servo.class, "sTumbler");
		rotatorServo = hardwareMap.get(Servo.class, "rotator");
		clawServo = hardwareMap.get(Servo.class, "claw");
		scoreTumbler = hardwareMap.get(Servo.class, "scoreTumbler");
		scoreClaw = hardwareMap.get(Servo.class, "scoreClaw");
		smallTumbler.setDirection(Servo.Direction.REVERSE);

		extendoServo.setPosition(Constants.EXTENDO_RETRACTED);
		tumblerServo.setPosition(Constants.TUMBLER_IDLE);
		smallTumbler.setPosition(Constants.SMALL_TUMBLER_IDLE);
		rotatorServo.setPosition(Constants.ROTATOR_BASE);
		clawServo.setPosition(Constants.CLAW_IDLE);
		scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
		scoreClaw.setPosition(Constants.SCORE_CLAW_IDLE);
	}

	@Override
	protected void OnRun()
	{
		Suspender();
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
		mecanumDrive.setDrivePowers(
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
			rotatorValue -= (float) (driveInput.getValue(Keybindings.Drive.ROTATOR_ANGLE) / 300f);
			rotatorValue = Utilities.Clamp(rotatorValue, 0, 1);
			rotatorServo.setPosition(rotatorValue);
		}

		if (driveInput.wasPressedThisFrame(Keybindings.Drive.EXTENDO_FULL_KEY)) {
			extendoValue = Constants.EXTENDO_EXTENDED;
			extendoServo.setPosition(extendoValue);
			tumblerServo.setPosition(Constants.TUMBLER_READY);
			smallTumbler.setPosition(Constants.SMALL_TUMBLER_BUSY);
			wallPicking = false;
		} else if (driveInput.wasPressedThisFrame(Keybindings.Drive.EXTENDO_HALF_KEY)) {
			extendoValue = (Constants.EXTENDO_RETRACTED + Constants.EXTENDO_EXTENDED) / 2f;
			extendoServo.setPosition(extendoValue);
			tumblerServo.setPosition(Constants.TUMBLER_READY);
			smallTumbler.setPosition(Constants.SMALL_TUMBLER_BUSY);
			wallPicking = false;
		} else if (driveInput.wasPressedThisFrame(Keybindings.Drive.EXTENDO_ZERO_KEY)) {
			extendoValue = Constants.EXTENDO_RETRACTED;
			extendoServo.setPosition(extendoValue);
			tumblerServo.setPosition(Constants.TUMBLER_READY);
			smallTumbler.setPosition(Constants.SMALL_TUMBLER_BUSY);
			wallPicking = false;
		} else if (driveInput.isPressed(Keybindings.Drive.EXTENDO_WALL_PICKUP)) {
			wallPicking = true;
			extendoValue = Constants.EXTENDO_RETRACTED;
			extendoServo.setPosition(extendoValue);
			tumblerServo.setPosition(Constants.TUMBLER_WALL);
			smallTumbler.setPosition(Constants.SMALL_TUMBLER_WALL);
		}

		if (!driveInput.wasPressedThisFrame(Keybindings.Drive.PRIMARY_KEY)) return;
		if (extendoState == Utilities.State.IDLE) {
			extendoState = Utilities.State.BUSY;
			rotatorLocked = false;

			if (scorerState == Utilities.State.IDLE)
				loaded = false; // If the scorer is not busy and the extendo is busy, the claw is not loaded

			extendoServo.setPosition(extendoValue);
			tumblerServo.setPosition(Constants.TUMBLER_READY);
			smallTumbler.setPosition(Constants.SMALL_TUMBLER_BUSY);
		} else if (extendoState == Utilities.State.BUSY) {
			rotatorLocked = true;

			scoreClaw.setPosition(Constants.SCORE_CLAW_IDLE); // In case the claw is closed
			if (!wallPicking)
				tumblerServo.setPosition(Constants.TUMBLER_PICKUP);
			setTimeout(() -> {
				clawServo.setPosition(Constants.CLAW_BUSY);
				setTimeout(() -> {
					extendoServo.setPosition(Constants.EXTENDO_RETRACTED);
					tumblerServo.setPosition(Constants.TUMBLER_READY);
					smallTumbler.setPosition(Constants.SMALL_TUMBLER_IDLE);
					rotatorServo.setPosition(Constants.ROTATOR_BASE);
					rotatorValue = Constants.ROTATOR_BASE;
					setTimeout(() -> {
						tumblerServo.setPosition(Constants.TUMBLER_DELIVER);
						setTimeout(() -> {
							scoreClaw.setPosition(Constants.SCORE_CLAW_BUSY);
							clawServo.setPosition(Constants.CLAW_IDLE);
							setTimeout(() -> {
								tumblerServo.setPosition(Constants.TUMBLER_IDLE);

								extendoState = Utilities.State.IDLE;
								loaded = true; // If the extendo is idle, the claw should be loaded
							}, 500);
						}, 500);
					}, 100);
				}, 500);
			}, 400);
		}
	}

	private void Scorer()
	{
		if (armInput.wasPressedThisFrame(Keybindings.Arm.BASKET_LOW_KEY)) liftLevel = 0;
		else if (armInput.wasPressedThisFrame(Keybindings.Arm.BASKET_HIGH_KEY)) liftLevel = 1;
		else if (armInput.wasPressedThisFrame(Keybindings.Arm.CHAMBER_HIGH_KEY)) liftLevel = 2;
		else if (armInput.wasPressedThisFrame(Keybindings.Arm.CHAMBER_ZERO_KEY)) liftLevel = 3;

		if (scorerState == Utilities.State.BUSY) {
			lift1.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);
			lift2.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);
			lift3.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);
		} else {
			lift1.setTargetPosition(Constants.LIFT_IDLE);
			lift2.setTargetPosition(Constants.LIFT_IDLE);
			lift3.setTargetPosition(Constants.LIFT_IDLE);
		}

		if (!loaded
			|| !armInput.wasPressedThisFrame(Keybindings.Arm.PRIMARY_KEY)
			|| liftLevel == 3)
			return;
		if (scorerState == Utilities.State.IDLE) {
			scorerState = Utilities.State.BUSY;

			lift1.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);
			lift2.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);
			lift3.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);
			scoreClaw.setPosition(Constants.SCORE_CLAW_BUSY);
			setTimeout(() -> scoreTumbler.setPosition(Constants.SCORE_TUMBLER_BUSY), 500 + 250 * (liftLevel >= 1 ? 1 : 0));
		} else if (scorerState == Utilities.State.BUSY) {
			if (liftLevel < 2) {
				scoreClaw.setPosition(Constants.SCORE_CLAW_IDLE);
				setTimeout(() -> {
					scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
					setTimeout(() -> {
						lift1.setTargetPosition(Constants.LIFT_IDLE);
						lift2.setTargetPosition(Constants.LIFT_IDLE);
						lift3.setTargetPosition(Constants.LIFT_IDLE);

						scorerState = Utilities.State.IDLE;
						loaded = false; // Once the scoring is done, the claw is no longer loaded
					}, 1000);
				}, 500);
			} else {
				lift1.setTargetPosition(Constants.LIFT_LEVELS[liftLevel] - Constants.LIFT_OFFSET);
				lift2.setTargetPosition(Constants.LIFT_LEVELS[liftLevel] - Constants.LIFT_OFFSET);
				lift3.setTargetPosition(Constants.LIFT_LEVELS[liftLevel] - Constants.LIFT_OFFSET);
				setTimeout(() -> {
					scoreClaw.setPosition(Constants.SCORE_CLAW_IDLE);
					setTimeout(() -> {
						scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
						setTimeout(() -> {
							lift1.setTargetPosition(Constants.LIFT_IDLE);
							lift2.setTargetPosition(Constants.LIFT_IDLE);
							lift3.setTargetPosition(Constants.LIFT_IDLE);

							scorerState = Utilities.State.IDLE;
							loaded = false; // Once the scoring is done, the claw is no longer loaded
						}, 500);
					}, 500);
				}, 500);
			}
		}
	}


	private Utilities.State suspendState = Utilities.State.IDLE;

	private void Suspender()
	{
		if (armInput.wasPressedThisFrame(Keybindings.Arm.CANCEL_SUSPEND_KEY) && suspendState == Utilities.State.BUSY) {
			lift1.setTargetPosition(Constants.LIFT_IDLE);
			lift2.setTargetPosition(Constants.LIFT_IDLE);
			lift3.setTargetPosition(Constants.LIFT_IDLE);
			suspendState = Utilities.State.IDLE;
			return;
		}

		if (!armInput.wasPressedThisFrame(Keybindings.Arm.SUSPEND_KEY)) return;

		if (suspendState == Utilities.State.IDLE) {
			lift1.setTargetPosition(Constants.LIFT_SUSPEND);
			lift2.setTargetPosition(Constants.LIFT_SUSPEND);
			lift3.setTargetPosition(Constants.LIFT_SUSPEND);
			suspendState = Utilities.State.BUSY;
		} else {
			lift1.setTargetPosition(Constants.LIFT_IDLE);
			lift2.setTargetPosition(Constants.LIFT_IDLE);
			lift3.setTargetPosition(Constants.LIFT_IDLE);
		}
	}

	@Override
	protected void OnTelemetry(Telemetry telemetry)
	{
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