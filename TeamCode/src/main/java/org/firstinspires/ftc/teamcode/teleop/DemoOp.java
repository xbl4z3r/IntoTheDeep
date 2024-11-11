package org.firstinspires.ftc.teamcode.teleop;

import static org.firstinspires.ftc.teamcode.Utilities.setTimeout;

import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
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
	private DcMotorEx lift1, lift2;
	private Servo extendoServo, tumblerServo, smallTumbler, rotatorServo, clawServo, scoreTumbler, scoreClaw;

	private static class Constants
	{
		public static float EXTENDO_RETRACTED = 0.5f;
		public static float EXTENDO_EXTENDED = 1f;

		public static float TUMBLER_IDLE = 0.9f;
		public static float TUMBLER_BUSY = 0.25f;
		public static float TUMBLER_OFFSET = 0.15f; // The amount of position to subtract to obtain the grabbing position
		public static float SMALL_TUMBLER_IDLE = 0.1f;
		public static float SMALL_TUMBLER_BUSY = 1f;

		public static float ROTATOR_BASE = 0.5f;

		public static float CLAW_IDLE = 0.9f;
		public static float CLAW_BUSY = 0.5f;

		public static int LIFT_IDLE = 0;
		public static int LIFT_SUSPEND = 1400;

		// BASKET_LOW, BASKET_HIGH, SUBMERSIBLE_HIGH
		public static int[] LIFT_LEVELS = {900, 2300, 760};

		public static float SCORE_TUMBLER_IDLE = 1;
		public static float SCORE_TUMBLER_BUSY = 0f;

		public static float SCORE_CLAW_IDLE = 0.3f;
		public static float SCORE_CLAW_BUSY = 0;
	}

	private static class Keybindings
	{
		public static class Arm
		{
			public static final InputSystem.Key BASKET_LOW_KEY = new InputSystem.Key("dpad_down");
			public static final InputSystem.Key BASKET_HIGH_KEY = new InputSystem.Key("dpad_up");
			public static final InputSystem.Key SUBMERSIBLE_HIGH_KEY = new InputSystem.Key("dpad_left");
			public static final InputSystem.Key PRIMARY_KEY = new InputSystem.Key("a");
			public static final InputSystem.Key SUSPEND_KEY = new InputSystem.Key("y");
			public static final InputSystem.Key CANCEL_SUSPEND_KEY = new InputSystem.Key("x");
		}

		public static class Drive
		{
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
		driveInput = new InputSystem(gamepad1);
		armInput = new InputSystem(gamepad2);

		mecanumDrive = new MecanumDrive(hardwareMap);

		lift1 = hardwareMap.get(DcMotorEx.class, "lift1");
		lift2 = hardwareMap.get(DcMotorEx.class, "lift2");
		lift1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift1.setTargetPosition(0);
		lift2.setTargetPosition(0);
		lift1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift1.setPower(1);
		lift2.setPower(1);

		extendoServo = hardwareMap.get(Servo.class, "extendo");
		tumblerServo = hardwareMap.get(Servo.class, "tumbler");
		smallTumbler = hardwareMap.get(Servo.class, "sTumbler");
		rotatorServo = hardwareMap.get(Servo.class, "rotator");
		clawServo = hardwareMap.get(Servo.class, "claw");
		scoreTumbler = hardwareMap.get(Servo.class, "scoreTumbler");
		scoreClaw = hardwareMap.get(Servo.class, "scoreClaw");

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
		if(suspendState == Utilities.State.BUSY) return;
		Lift();
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
						driveInput.getValue(Keybindings.Drive.DRIVE_X)).times(speed),
						-driveInput.getValue(Keybindings.Drive.DRIVE_ROT_L) + driveInput.getValue(Keybindings.Drive.DRIVE_ROT_R)
				)
		);
	}

	private boolean armReady = false;

	private Utilities.State armState = Utilities.State.IDLE;
	private Utilities.State clawState = Utilities.State.IDLE;
	private float rotatorValue = Constants.ROTATOR_BASE;

	private void Extendo()
	{
		if (clawState == Utilities.State.IDLE) {
			rotatorValue += driveInput.getValue(Keybindings.Drive.ROTATOR_ANGLE) / 10f;
			rotatorValue = Utilities.Clamp(rotatorValue, 0, 1);
			rotatorServo.setPosition(rotatorValue);
		}

		// Grab
		if (!driveInput.wasPressedThisFrame(Keybindings.Drive.PRIMARY_KEY)) return;
		if (armState == Utilities.State.IDLE) {
			armState = Utilities.State.BUSY;
			clawState = Utilities.State.IDLE;

			clawServo.setPosition(Constants.CLAW_IDLE);
			setTimeout(() -> {
				extendoServo.setPosition(Constants.EXTENDO_EXTENDED);
				tumblerServo.setPosition(Constants.TUMBLER_BUSY);
				smallTumbler.setPosition(Constants.SMALL_TUMBLER_BUSY);
			}, 500);
		} else if (armState == Utilities.State.BUSY) {
			armState = Utilities.State.IDLE;
			tumblerServo.setPosition(Constants.TUMBLER_BUSY - Constants.TUMBLER_OFFSET);
			setTimeout(() -> {
				clawState = Utilities.State.BUSY;
				clawServo.setPosition(Constants.CLAW_BUSY);
				setTimeout(() -> {
					extendoServo.setPosition(Constants.EXTENDO_RETRACTED);
					tumblerServo.setPosition(Constants.TUMBLER_BUSY);
					smallTumbler.setPosition(Constants.SMALL_TUMBLER_IDLE);
					rotatorServo.setPosition(Constants.ROTATOR_BASE);
					rotatorValue = Constants.ROTATOR_BASE;
					setTimeout(() -> {
						tumblerServo.setPosition(Constants.TUMBLER_IDLE);
						setTimeout(() -> {
							scoreClaw.setPosition(Constants.SCORE_CLAW_BUSY);
							setTimeout(() -> {
								clawServo.setPosition(Constants.CLAW_IDLE);
								armReady = true;
							}, 600);
						}, 500);
					}, 500);
				}, 500);
			}, 400);
		}
	}

	private Utilities.State liftState = Utilities.State.IDLE;
	private int liftLevel = 0;

	private void Lift()
	{
		if (armInput.wasPressedThisFrame(Keybindings.Arm.BASKET_LOW_KEY)) liftLevel = 0;
		else if (armInput.wasPressedThisFrame(Keybindings.Arm.BASKET_HIGH_KEY)) liftLevel = 1;
		else if (armInput.wasPressedThisFrame(Keybindings.Arm.SUBMERSIBLE_HIGH_KEY)) liftLevel = 2;

		if (liftState == Utilities.State.IDLE) {
			if (lift1.getTargetPosition() != Constants.LIFT_IDLE)
				lift1.setTargetPosition(Constants.LIFT_IDLE);
			if (lift2.getTargetPosition() != Constants.LIFT_IDLE)
				lift2.setTargetPosition(Constants.LIFT_IDLE);
		} else if (liftState == Utilities.State.BUSY) {
			lift1.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);
			lift2.setTargetPosition(Constants.LIFT_LEVELS[liftLevel]);
		}
	}

	private Utilities.State suspendState = Utilities.State.IDLE;

	private void Suspender()
	{
		if (armInput.wasPressedThisFrame(Keybindings.Arm.CANCEL_SUSPEND_KEY) && suspendState == Utilities.State.BUSY) {
			lift1.setTargetPosition(Constants.LIFT_IDLE);
			lift2.setTargetPosition(Constants.LIFT_IDLE);
			suspendState = Utilities.State.IDLE;
			return;
		}

		if (!armInput.wasPressedThisFrame(Keybindings.Arm.SUSPEND_KEY)) return;

		if(suspendState == Utilities.State.IDLE) {
			lift1.setTargetPosition(Constants.LIFT_SUSPEND);
			lift2.setTargetPosition(Constants.LIFT_SUSPEND);
			suspendState = Utilities.State.BUSY;
		} else {
			lift1.setTargetPosition(Constants.LIFT_IDLE);
			lift2.setTargetPosition(Constants.LIFT_IDLE);
		}
	}

	private void Scorer()
	{
		if (!armReady) return;
		if (!armInput.wasPressedThisFrame(Keybindings.Arm.PRIMARY_KEY)) return;
		if (liftState == Utilities.State.IDLE) {
			liftState = Utilities.State.BUSY;
			scoreClaw.setPosition(Constants.SCORE_CLAW_BUSY);
			setTimeout(() -> scoreTumbler.setPosition(Constants.SCORE_TUMBLER_BUSY), 2000);
		} else if (liftState == Utilities.State.BUSY) {
			scoreClaw.setPosition(Constants.SCORE_CLAW_IDLE);
			setTimeout(() -> {
				scoreTumbler.setPosition(Constants.SCORE_TUMBLER_IDLE);
				setTimeout(() -> {
					lift1.setTargetPosition(Constants.LIFT_IDLE);
					lift2.setTargetPosition(Constants.LIFT_IDLE);

					liftState = Utilities.State.IDLE;
					armReady = false;
				}, 200);
			}, 500);
		}
	}

	@Override
	protected void OnTelemetry(Telemetry telemetry)
	{
		telemetry.addLine("[SYSTEM] Op mode running...");
		if(suspendState == Utilities.State.BUSY) {
			telemetry.addLine("[INFO] In Suspend Mode");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.SUSPEND_KEY + " again to suspend");
			telemetry.addLine("[INFO] Press " + Keybindings.Arm.CANCEL_SUSPEND_KEY + " to cancel");
		} else {
			telemetry.addLine("[INFO] Welcome Drivers!");
			telemetry.addData("[INFO] Press " + Keybindings.Arm.SUSPEND_KEY, " to enter Suspend Mode");
			telemetry.addData("[INFO] Press " + Keybindings.Arm.BASKET_LOW_KEY, " to move to Basket Low");
			telemetry.addData("[INFO] Press " + Keybindings.Arm.BASKET_HIGH_KEY, " to move to Basket High");
			telemetry.addData("[INFO] Press " + Keybindings.Arm.SUBMERSIBLE_HIGH_KEY, " to move to Submersible High");
			telemetry.addData("[INFO] Press " + Keybindings.Arm.PRIMARY_KEY, " to score");
		}
	}
}
