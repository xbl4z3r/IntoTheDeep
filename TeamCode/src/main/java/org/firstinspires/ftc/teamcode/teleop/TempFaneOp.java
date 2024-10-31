package org.firstinspires.ftc.teamcode.teleop;

import static org.firstinspires.ftc.teamcode.Utilities.setTimeout;

import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.Utilities;

@TeleOp(name = "Temp \uD83D\uDC79Fane\uD83D\uDC79 Op ", group = "TeleOp")
public class TempFaneOp extends LinearOpMode
{
	private Gamepad wheelsGamepad, armGamepad;
	private MecanumDrive mecanumDrive;
	private DcMotorEx lift;
	private Servo extendoServo1, extendoServo2, tumblerServo, smallTumbler, rotatorServo, clawServo, scoreTumbler, scoreClaw;

	public static class Constants
	{
		public static float extendo1Retracted = 1;
		public static float extendo2Retracted = 0;
		public static float extendo1Extended = 0;
		public static float extendo2Extended = 1;

		public static float tumblerIdle = 1;
		public static float tumblerBusy = 0.2f;

		public static float smallTumblerIdle = 0;
		public static float smallTumblerBusy = 1;

		public static float rotatorLeft = 0.25f;
		public static float rotatorCenter = 0.5f;
		public static float rotatorRight = 0.75f;

		public static float clawIdle = 1;
		public static float clawBusy = 0.2f;

		public static int liftLoading = 0;
		public static int[] liftLevels = { 0, 0, 0, 0 };

		public static float scoreTumblerIdle = 0;
		public static float scoreTumblerBasket = 0.75f;

		public static float scoreClawIdle = 0;
		public static float scoreClawBusy = 1;
	}


	@Override
	public void runOpMode() throws InterruptedException
	{
		Init();
		waitForStart();
		Run();
	}

	private void Init()
	{
		wheelsGamepad = gamepad1;
		armGamepad = gamepad2;

		mecanumDrive = new MecanumDrive(hardwareMap);

		lift = hardwareMap.get(DcMotorEx.class, "lift");
		lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
		//lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		//lift.setTargetPosition(0);
		//lift.setPower(0.8);


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

	private void Run()
	{
		while (!isStopRequested())
		{
			Wheels();
			Diver();

			Lift();

			Telemetry();
		}
	}

	private void Wheels()
	{
		float speed = 0.7f;
		if (wheelsGamepad.right_bumper)
			speed = 1.0f; // turbo
		else if (wheelsGamepad.left_bumper)
			speed = 0.4f; // suppress
		mecanumDrive.setDrivePowers(new PoseVelocity2d(new Vector2d(wheelsGamepad.left_stick_y, wheelsGamepad.left_stick_x).times(speed),
				wheelsGamepad.left_trigger - wheelsGamepad.right_trigger));
	}

	private Utilities.State armState = Utilities.State.IDLE;
	private Utilities.State clawState = Utilities.State.IDLE;
	private Utilities.State scoreClawState = Utilities.State.IDLE;
	private int rotatorState = 1; // 0 left, 1 center, 2 right
	private boolean rotatorSafe = false;
	private void Diver()
	{
		// Rotator
		if (gamepad1.left_bumper && rotatorState > 0 && !rotatorSafe)
		{
			rotatorSafe = true;
			rotatorState--;
		}
		else if (gamepad1.right_bumper && rotatorState < 2 && !rotatorSafe)
		{
			rotatorSafe = true;
			rotatorState++;
		}
		if (!gamepad1.left_bumper && !gamepad1.right_bumper) rotatorSafe = false;

		if (clawState == Utilities.State.IDLE)
		{
			switch (rotatorState)
			{
				case 0: rotatorServo.setPosition(Constants.rotatorLeft);   break;
				case 1: rotatorServo.setPosition(Constants.rotatorCenter); break;
				case 2: rotatorServo.setPosition(Constants.rotatorRight);  break;
				default:
					break;
			}
		}

		// Grab
		if (gamepad1.x && armState == Utilities.State.IDLE)
		{
			armState = Utilities.State.BUSY;
			clawState = Utilities.State.IDLE;

			clawServo.setPosition(Constants.clawIdle);
			setTimeout(() -> {
				extendoServo1.setPosition(Constants.extendo1Extended);
				extendoServo2.setPosition(Constants.extendo2Extended);
				tumblerServo.setPosition(Constants.tumblerBusy);
				smallTumbler.setPosition(Constants.smallTumblerBusy);
			}, 500);
		}
		else if (gamepad1.a && armState == Utilities.State.BUSY)
		{
			armState = Utilities.State.IDLE;
			clawState = Utilities.State.BUSY;

			rotatorState = 1;

			clawServo.setPosition(Constants.clawBusy);
			setTimeout(() -> {
				extendoServo1.setPosition(Constants.extendo1Retracted);
				extendoServo2.setPosition(Constants.extendo2Retracted);
				tumblerServo.setPosition(Constants.tumblerIdle);
				smallTumbler.setPosition(Constants.smallTumblerBusy);
				rotatorServo.setPosition(Constants.rotatorCenter);
			}, 500);
		}
	}

	private Utilities.State liftState = Utilities.State.IDLE;
	private int liftLevel = 0;
	private boolean scoreSafe = false;
	private void Lift()
	{
		lift.setPower(armGamepad.right_stick_y);

		if (armGamepad.dpad_down) //
			liftLevel = 0;
		else if (armGamepad.dpad_right) //
			liftLevel = 1;
		else if (armGamepad.dpad_left) //
			liftLevel = 2;
		else if (armGamepad.dpad_up) //
			liftLevel = 3;

		if (liftState == Utilities.State.IDLE)
		{
			if (armGamepad.a && !scoreSafe)
			{
				liftState = Utilities.State.BUSY;

				scoreClaw.setPosition(Constants.scoreClawBusy);
				scoreClawState = Utilities.State.BUSY;

				scoreTumbler.setPosition(Constants.scoreTumblerBasket);

				scoreSafe = true;
			}
			else scoreSafe = armGamepad.a;
		}
		else // BUSY
		{
//			lift.setTargetPosition(Constants.liftLevels[liftLevel]);

			if (armGamepad.a && !scoreSafe)
			{
				scoreClaw.setPosition(Constants.scoreClawIdle);
				setTimeout(() -> {
					scoreTumbler.setPosition(Constants.scoreTumblerIdle);
					setTimeout(() -> {
//						lift.setTargetPosition(Constants.liftLoading);

						scoreClawState = Utilities.State.IDLE;
						liftState = Utilities.State.IDLE;
					}, 1500);
				}, 300);

				scoreSafe = true;
			}
			else scoreSafe = armGamepad.a;
		}
	}

	private void Telemetry()
	{
		telemetry.addData("Arm        State: ", armState.name());
		telemetry.addData("Rotator    State: ", rotatorState);
		telemetry.addData("Claw       State: ", clawState.name());
		telemetry.addData("Score Claw State: ", clawState.name());

		telemetry.addData("Lift Pow:         ", lift.getPower());
		telemetry.addData("Lift Pos:         ", lift.getCurrentPosition());

		telemetry.update();
	}
}