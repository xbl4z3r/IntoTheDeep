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

@TeleOp(name = "Fane Op", group = "TeleOp")
public final class FaneOp extends BaseOpMode
{
	private InputSystem driveInput, armInput;
	private MecanumDrive mecanumDrive;
	private DcMotorEx lift;
	private Servo extendoServo1, extendoServo2, tumblerServo, smallTumbler, rotatorServo, clawServo, scoreTumbler, scoreClaw;

	private static class Constants
	{
		public static float extendo1Retracted = 0;
		public static float extendo2Retracted = 1;
		public static float extendo1Extended = 1;
		public static float extendo2Extended = 0;

		public static float tumblerIdle = 0.17f;
		public static float tumblerBusy = 0.7f;
		public static float tumblerOffset = 0.15f; // Max - Busy
		public static float smallTumblerIdle = 1;
		public static float smallTumblerBusy = 0.5f;

		public static float rotatorLeft = 0.25f;
		public static float rotatorCenter = 0.5f;
		public static float rotatorRight = 0.75f;

		public static float clawIdle = 0.6f;
		public static float clawBusy = 0.2f;

		public static int liftLoading = 0;
		public static int liftOffset = 0;
		public static int[] liftLevels = {0, 330, 550, 1100};

		public static float scoreTumblerIdle = 0;
		public static float scoreTumblerBasket = 0.75f;

		public static float scoreClawIdle = 1;
		public static float scoreClawBusy = 0;
	}

	private static class Keybindings
	{
		public static final InputSystem.Key TURBO_KEY = new InputSystem.Key("right_bumper");
		public static final InputSystem.Key SUPPRESS_KEY = new InputSystem.Key("left_bumper");
		public static final InputSystem.Axis DRIVE_X = new InputSystem.Axis("left_stick_x");
		public static final InputSystem.Axis DRIVE_Y = new InputSystem.Axis("left_stick_y");
		public static final InputSystem.Axis DRIVE_ROT_L = new InputSystem.Axis("left_trigger");
		public static final InputSystem.Axis DRIVE_ROT_R = new InputSystem.Axis("right_trigger");
	}

	@Override
	protected void OnInitialize()
	{
		driveInput = new InputSystem(gamepad1);
		armInput = new InputSystem(gamepad2);

		mecanumDrive = new MecanumDrive(hardwareMap);

		lift = hardwareMap.get(DcMotorEx.class, "lift");
		lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift.setTargetPosition(0);
		lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift.setPower(1);

		extendoServo1 = hardwareMap.get(Servo.class, "extendo1");
		extendoServo2 = hardwareMap.get(Servo.class, "extendo2");
		tumblerServo = hardwareMap.get(Servo.class, "tumbler");
		smallTumbler = hardwareMap.get(Servo.class, "sTumbler");
		rotatorServo = hardwareMap.get(Servo.class, "rotator");
		clawServo = hardwareMap.get(Servo.class, "claw");
		scoreTumbler = hardwareMap.get(Servo.class, "scoreTumbler");
		scoreClaw = hardwareMap.get(Servo.class, "scoreClaw");

		extendoServo1.setPosition(Constants.extendo1Retracted);
		extendoServo2.setPosition(Constants.extendo2Retracted);
		tumblerServo.setPosition(Constants.tumblerIdle);
		smallTumbler.setPosition(Constants.smallTumblerIdle);
		rotatorServo.setPosition(Constants.rotatorCenter);
		clawServo.setPosition(Constants.clawIdle);
		scoreTumbler.setPosition(Constants.scoreTumblerIdle);
		scoreClaw.setPosition(Constants.scoreClawIdle);
	}

	@Override
	protected void OnRun()
	{
		Drive();
		Diver();
		Lift();
	}

	private void Drive()
	{
		float speed = 0.7f;
		if (driveInput.isPressed(Keybindings.TURBO_KEY)) speed = 1.0f;
		else if (driveInput.isPressed(Keybindings.SUPPRESS_KEY)) speed = 0.4f;
		mecanumDrive.setDrivePowers(
				new PoseVelocity2d(new Vector2d(driveInput.getValue(Keybindings.DRIVE_Y),
						driveInput.getValue(Keybindings.DRIVE_X)).times(speed),
						-driveInput.getValue(Keybindings.DRIVE_ROT_L) + driveInput.getValue(Keybindings.DRIVE_ROT_R)
				)
		);
	}

	private Utilities.State armState = Utilities.State.IDLE;
	private Utilities.State clawState = Utilities.State.IDLE;
	private Utilities.State scoreClawState = Utilities.State.IDLE;
	private int rotatorState = 1; // 0 left, 1 center, 2 right
	private boolean rotatorSafe = false;

	private void Diver()
	{
		// Rotator
		if (gamepad1.right_bumper && rotatorState > 0 && !rotatorSafe) {
			rotatorSafe = true;
			rotatorState--;
		} else if (gamepad1.left_bumper && rotatorState < 2 && !rotatorSafe) {
			rotatorSafe = true;
			rotatorState++;
		}
		if (!gamepad1.left_bumper && !gamepad1.right_bumper) rotatorSafe = false;

		if (clawState == Utilities.State.IDLE) {
			switch (rotatorState) {
				case 0:
					rotatorServo.setPosition(Constants.rotatorLeft);
					break;
				case 1:
					rotatorServo.setPosition(Constants.rotatorCenter);
					break;
				case 2:
					rotatorServo.setPosition(Constants.rotatorRight);
					break;
				default:
					break;
			}
		}

		// Grab
		if (gamepad1.x && armState == Utilities.State.IDLE) {
			armState = Utilities.State.BUSY;
			clawState = Utilities.State.IDLE;

			clawServo.setPosition(Constants.clawIdle);
			setTimeout(() -> {
				extendoServo1.setPosition(Constants.extendo1Extended);
				extendoServo2.setPosition(Constants.extendo2Extended);
				tumblerServo.setPosition(Constants.tumblerBusy);
				smallTumbler.setPosition(Constants.smallTumblerBusy);
			}, 500);
		} else if (gamepad1.a && armState == Utilities.State.BUSY) {
			armState = Utilities.State.IDLE;
			tumblerServo.setPosition(Constants.tumblerBusy + Constants.tumblerOffset);
			setTimeout(() -> {
				clawState = Utilities.State.BUSY;
				rotatorState = 1;
				clawServo.setPosition(Constants.clawBusy);
				setTimeout(() -> {
					extendoServo1.setPosition(Constants.extendo1Retracted);
					extendoServo2.setPosition(Constants.extendo2Retracted);
					tumblerServo.setPosition(Constants.tumblerIdle - Constants.tumblerOffset);
					smallTumbler.setPosition(Constants.smallTumblerIdle);
					rotatorServo.setPosition(Constants.rotatorCenter);
					setTimeout(() -> {
						scoreClaw.setPosition(Constants.scoreClawBusy);
						setTimeout(() -> {
							clawServo.setPosition(Constants.clawIdle);
							setTimeout(() -> tumblerServo.setPosition(Constants.tumblerIdle),
									400);
						}, 500);
					}, 800);
				}, 500);
			}, 300);
		}
	}

	private Utilities.State liftState = Utilities.State.IDLE;
	private int liftLevel = 0;
	private boolean scoreSafe = false;

	private void Lift()
	{
		if (gamepad2.dpad_down) //
			liftLevel = 0;
		else if (gamepad2.dpad_right) //
			liftLevel = 1;
		else if (gamepad2.dpad_left) // low basket
			liftLevel = 2;
		else if (gamepad2.dpad_up) // high basket
			liftLevel = 3;

		if (liftState == Utilities.State.IDLE) {
			if (lift.getTargetPosition() != Constants.liftLoading)
				lift.setTargetPosition(Constants.liftLoading);

			if (gamepad2.a && !scoreSafe) {
				liftState = Utilities.State.BUSY;

				scoreClaw.setPosition(Constants.scoreClawBusy);
				scoreClawState = Utilities.State.BUSY;

				setTimeout(() -> scoreTumbler.setPosition(Constants.scoreTumblerBasket), 2000);

				scoreSafe = true;
			} else scoreSafe = gamepad2.a;
		} else // BUSY
		{
			lift.setTargetPosition(Constants.liftLevels[liftLevel] - Constants.liftOffset);

			if (gamepad2.a && !scoreSafe) {
				if (liftLevel > 1) {
					scoreClaw.setPosition(Constants.scoreClawIdle);
					setTimeout(() -> {
						scoreTumbler.setPosition(Constants.scoreTumblerIdle);
						setTimeout(() -> {
							lift.setTargetPosition(Constants.liftLoading);

							scoreClawState = Utilities.State.IDLE;
							liftState = Utilities.State.IDLE;
						}, 1500);
					}, 500);
				} else if (liftLevel == 1) {
					Constants.liftOffset = 250;
					setTimeout(() -> {
						scoreClaw.setPosition(Constants.scoreClawIdle);
						setTimeout(() -> {
							scoreTumbler.setPosition(Constants.scoreTumblerIdle);
							setTimeout(() -> {
								lift.setTargetPosition(Constants.liftLoading);
								scoreClawState = Utilities.State.IDLE;
								liftState = Utilities.State.IDLE;
								Constants.liftOffset = 0;
							}, 500);
						}, 500);
					}, 200);
				}
				scoreSafe = true;
			} else scoreSafe = armInput.wasPressedThisFrame("a");
		}
	}

	@Override
	protected void OnTelemetry(Telemetry telemetry)
	{
//		telemetry.addData("Arm        State: ", armState.name());
//		telemetry.addData("Rotator    State: ", rotatorState);
//		telemetry.addData("Claw       State: ", clawState.name());
//		telemetry.addData("Score Claw State: ", clawState.name());
//
//		telemetry.addData("Lift Pow:         ", lift.getPower());
//		telemetry.addData("Lift Level:       ", liftLevel);
//
//		telemetry.update();
	}
}
