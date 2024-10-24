package org.firstinspires.ftc.teamcode.teleop;

import static org.firstinspires.ftc.teamcode.Utilities.WaitToFinish;
import static org.firstinspires.ftc.teamcode.Utilities.setTimeout;

import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.BuildConfig;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.Utilities;

import java.util.zip.CheckedInputStream;


@TeleOp(name = "\uD83C\uDD98⚠️ Temp Op ⚠️\uD83C\uDD98", group = "TeleOp")
public final class TempTeleOp extends LinearOpMode
{
	Gamepad wheelsGamepad, armGamepad;

	MecanumDrive mecanumDrive;

	DcMotorEx tumbler, lift1, lift2;

	Servo rotator, bigAngler, smallAngler, pickupClaw, scoreClaw, scoreTumbler;

	public static class Constants
	{
		public static float rotatorIdle = 0.33f;
		public static float rotatorBusy = 1.0f;

		public static float bigAnglerCenter = 0.38f;
		public static float smallAnglerCenter = 0.41f;

		public static float pickupClawIdle = 1.0f;
		public static float pickupClawBusy = 0.0f;

		public static float scoreTumblerIdle = 0.85f;
		public static float scoreTumblerBusy = 0.1f;

		public static float scoreClawIdle = 0.3f;
		public static float scoreClawBusy = 0.0f;

		public static int tumblerIdle = -700;
		public static int tumblerPickup = 0;
		public static int tumblerDeliver = -900;

		public static int liftLoading = 0;
		public static int[] liftLevels = { 0, 100, 800, 1950 };
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

		tumbler = hardwareMap.get(DcMotorEx.class, "tumbler");
		tumbler.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		tumbler.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		tumbler.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		tumbler.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT); // melc
		tumbler.setTargetPosition(Constants.tumblerIdle);
		tumbler.setPower(0.6);

		lift1 = hardwareMap.get(DcMotorEx.class, "lift1");
		lift2 = hardwareMap.get(DcMotorEx.class, "lift2");
		lift2.setDirection(DcMotorSimple.Direction.REVERSE);
		lift1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift1.setTargetPositionTolerance(100);
		lift2.setTargetPositionTolerance(100);
		lift1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		lift2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		lift1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift1.setTargetPosition(0);
		lift2.setTargetPosition(0);
		lift1.setPower(1.0);
		lift2.setPower(1.0);

		rotator = hardwareMap.get(Servo.class, "rotator");
		rotator.setPosition(Constants.rotatorIdle);
		bigAngler = hardwareMap.get(Servo.class, "bigAngler");
		bigAngler.setPosition(Constants.bigAnglerCenter);
		smallAngler = hardwareMap.get(Servo.class, "smallAngler");
		smallAngler.setPosition(Constants.smallAnglerCenter);
		pickupClaw = hardwareMap.get(Servo.class, "pickupClaw");
		pickupClaw.setPosition(Constants.pickupClawIdle);
		scoreClaw = hardwareMap.get(Servo.class, "scoreClaw");
		scoreClaw.setPosition(Constants.scoreClawIdle);
		scoreTumbler = hardwareMap.get(Servo.class, "scoreTumbler");
		scoreTumbler.setPosition(Constants.scoreTumblerIdle);
	}


	private void Run()
	{
		while (opModeIsActive() && !isStopRequested())
		{
			Wheels();
			Anglers();
			Pickup();
			Lift();
			SetMotorPowers();

			Telemetry();
		}
	}

	private void Wheels()
	{
		float speed = 0.7f;
		if (wheelsGamepad.right_bumper)
			speed = 1.0f; // turbo
		else if (wheelsGamepad.left_bumper)
			speed = 0.4f; // supress

		mecanumDrive.setDrivePowers(new PoseVelocity2d(new Vector2d(wheelsGamepad.left_stick_y, wheelsGamepad.left_stick_x).times(-speed),
				wheelsGamepad.left_trigger - wheelsGamepad.right_trigger));
	}

	private int bigAnglerPos = 1;
	private int smallAnglerPos = 1;
	private boolean bigAnglerSafe = false;
	private boolean smallAnglerSafe = false;
	private void Anglers()
	{
		// Big
		if (armGamepad.left_bumper && bigAnglerPos > 0 && !bigAnglerSafe)
		{
			bigAnglerSafe = true;
			bigAnglerPos--;
		}
		else if (armGamepad.right_bumper && bigAnglerPos < 2 && !bigAnglerSafe)
		{
			bigAnglerSafe = true;
			bigAnglerPos++;
		}
		if (!armGamepad.left_bumper && !armGamepad.right_bumper) bigAnglerSafe = false;


		// Small
		if (armGamepad.left_bumper && smallAnglerPos > 0 && !smallAnglerSafe)
		{
			smallAnglerSafe = true;
			smallAnglerPos--;
		}
		else if (armGamepad.right_bumper && smallAnglerPos < 2 && !smallAnglerSafe)
		{
			smallAnglerSafe = true;
			smallAnglerPos++;
		}
		if (!armGamepad.left_bumper && !armGamepad.right_bumper) smallAnglerSafe = false;


		if (pickupState == Utilities.State.BUSY)
		{
			switch (bigAnglerPos)
			{
				case 0:
					bigAngler.setPosition(Constants.bigAnglerCenter - 0.2);
					break;
				case 1:
					bigAngler.setPosition(Constants.bigAnglerCenter);
					break;
				case 2:
					bigAngler.setPosition(Constants.bigAnglerCenter + 0.2);
					break;
			}

			switch (smallAnglerPos)
			{
				case 0:
					smallAngler.setPosition(Constants.smallAnglerCenter - 0.2);
					break;
				case 1:
					smallAngler.setPosition(Constants.smallAnglerCenter);
					break;
				case 2:
					smallAngler.setPosition(Constants.smallAnglerCenter + 0.2);
					break;
			}
		}
		else
		{
			bigAngler.setPosition(Constants.bigAnglerCenter);
			smallAngler.setPosition(Constants.smallAnglerCenter);
		}
	}


	private boolean pickupSafe = false;
	private Utilities.State pickupState = Utilities.State.IDLE;
	private Utilities.State scoreClawState = Utilities.State.IDLE;
	private void Pickup()
	{
		if (wheelsGamepad.a && !pickupSafe)
		{
			pickupSafe = true;
			if (pickupState == Utilities.State.IDLE)
			{
				pickupState = Utilities.State.BUSY;

				pickupClaw.setPosition(Constants.pickupClawIdle);
				rotator.setPosition(Constants.rotatorIdle);
				tumbler.setTargetPosition(Constants.tumblerPickup);
			}
			else if (pickupState == Utilities.State.BUSY)
			{
				pickupClaw.setPosition(Constants.pickupClawBusy);
				setTimeout(500, () -> {
					rotator.setPosition(Constants.rotatorBusy);

					setTimeout(500, () -> {
						tumbler.setTargetPosition(Constants.tumblerDeliver);
						WaitToFinish(tumbler);

						scoreClaw.setPosition(Constants.scoreClawBusy);

						setTimeout(400, () -> {
							pickupClaw.setPosition(Constants.pickupClawIdle);

							setTimeout(300, () -> {
								tumbler.setTargetPosition(Constants.tumblerIdle);
								rotator.setPosition(Constants.rotatorIdle);


								pickupState = Utilities.State.IDLE;
								scoreClawState = Utilities.State.BUSY;
							});
						});
					});

				});
			}
		}
		else pickupSafe = wheelsGamepad.a;
	}

	private Utilities.State liftState = Utilities.State.IDLE;
	private int liftLevel = 0;
	private boolean scoreSafe = false;
	void Lift()
	{
		if (armGamepad.dpad_down) // lowest
			liftLevel = 0;
		else if (armGamepad.dpad_right) // high bar
			liftLevel = 1;
		else if (armGamepad.dpad_left) // low basket
			liftLevel = 2;
		else if (armGamepad.dpad_up) // high basket
			liftLevel = 3;

		if (liftState == Utilities.State.IDLE)
		{
			if (armGamepad.a && !scoreSafe)
			{
				liftState = Utilities.State.BUSY;

				scoreClaw.setPosition(Constants.scoreClawBusy);
				scoreClawState = Utilities.State.BUSY;

				scoreTumbler.setPosition(Constants.scoreTumblerBusy);

				scoreSafe = true;
			}
			else scoreSafe = armGamepad.a;
		}
		else // BUSY
		{
			lift1.setTargetPosition(Constants.liftLevels[liftLevel]);
			lift2.setTargetPosition(Constants.liftLevels[liftLevel]);

			if (armGamepad.a && !scoreSafe)
			{
				scoreClaw.setPosition(Constants.scoreClawIdle);
				setTimeout(300, () -> {
					scoreTumbler.setPosition(Constants.scoreTumblerIdle);
					setTimeout(1500, () -> {
						lift1.setTargetPosition(Constants.liftLoading);
						lift2.setTargetPosition(Constants.liftLoading);

						scoreClawState = Utilities.State.IDLE;
						liftState = Utilities.State.IDLE;
					});
				});

				scoreSafe = true;
			}
			else scoreSafe = armGamepad.a;
		}
	}

	private void SetMotorPowers()
	{
		if ((Math.abs(lift1.getTargetPosition() - lift1.getCurrentPosition()) > lift1.getTargetPositionTolerance()) ||
			(Math.abs(lift2.getTargetPosition() - lift2.getCurrentPosition()) > lift2.getTargetPositionTolerance()))
		{
			lift1.setPower(1.0);
			lift2.setPower(1.0);
		}
		else
		{
			lift1.setPower(0.05);
			lift2.setPower(0.05);
		}
	}


	private void Telemetry()
	{
		telemetry.addData("Pickup State:     ", pickupState.name());
		telemetry.addData("Score Claw State: ", scoreClawState.name());
		telemetry.addData("Tumbler Pose:     ", tumbler.getCurrentPosition());

		telemetry.addData("Lift State:       ", liftState.name());
		telemetry.addData("Lift Level:       ", liftLevel);
		telemetry.addData("Lift1 Pos:        ", lift1.getCurrentPosition());
		telemetry.addData("Lift2 Pos:        ", lift2.getCurrentPosition());
		telemetry.addData("Lift1 Pow:        ", lift1.getPower());
		telemetry.addData("Lift2 Pow:        ", lift2.getPower());

		telemetry.update();
	}
}