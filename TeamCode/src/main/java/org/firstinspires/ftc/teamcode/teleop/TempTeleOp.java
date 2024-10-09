package org.firstinspires.ftc.teamcode.teleop;

import static org.firstinspires.ftc.teamcode.Utilities.WaitToFinish;
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


@TeleOp(name = "\uD83C\uDD98⚠️ Temp Op ⚠️\uD83C\uDD98", group = "TeleOp")
public final class TempTeleOp extends LinearOpMode
{
	Gamepad wheelsGamepad, armGamepad;

	MecanumDrive mecanumDrive;

	DcMotorEx tumbler;

	Servo pickupClaw, rotator, scoreClaw;

	public static class Constants
	{
		public static float pickupClawIdle = 1.0f;
		public static float pickupClawBusy = 0.0f;
		public static float scoreClawIdle = 0.0f;
		public static float scoreClawBusy = 1.0f;
		public static float rotatorIdle = 0.0f;
		public static float rotatorBusy = 1.0f;

		public static int tumblerIdle = 200;
		public static int tumblerPickup = 1000;
		public static int tumblerDeliver = 0;

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
		tumbler.setTargetPosition(0);
		tumbler.setPower(0.2);

		pickupClaw = hardwareMap.get(Servo.class, "pickupClaw");
		pickupClaw.setPosition(Constants.pickupClawIdle);
		rotator = hardwareMap.get(Servo.class, "rotator");
		rotator.setPosition(Constants.rotatorIdle);
		scoreClaw = hardwareMap.get(Servo.class, "scoreClaw");
	}


	private void Run()
	{
		while (opModeIsActive() && !isStopRequested())
		{
			Wheels();
			Pickup();

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

		mecanumDrive.setDrivePowers(new PoseVelocity2d(new Vector2d(wheelsGamepad.left_stick_x, wheelsGamepad.left_stick_y).times(speed),
				wheelsGamepad.left_trigger - wheelsGamepad.right_trigger));
	}

	private boolean pickupSafe = false;
	private Utilities.State pickupState = Utilities.State.IDLE;
	private void Pickup()
	{
		if (wheelsGamepad.a && !pickupSafe)
		{
			pickupSafe = true;
			switch (pickupState)
			{
				case IDLE:
					pickupState = Utilities.State.BUSY;

					pickupClaw.setPosition(Constants.pickupClawIdle);
					rotator.setPosition(Constants.rotatorIdle);
					tumbler.setTargetPosition(Constants.tumblerPickup);

					break;

				case BUSY:

					pickupClaw.setPosition(Constants.pickupClawBusy);
					setTimeout(300, () -> {
						tumbler.setTargetPosition(Constants.tumblerDeliver);
						rotator.setPosition(Constants.rotatorBusy);

						WaitToFinish(tumbler);

						scoreClaw.setPosition(Constants.scoreClawBusy);
						pickupClaw.setPosition(Constants.pickupClawIdle);

						setTimeout(300,
								() -> tumbler.setTargetPosition(Constants.tumblerIdle));
					});

					pickupState = Utilities.State.IDLE;

					break;
			}
		}
		else pickupSafe = wheelsGamepad.a;
	}



	private void Telemetry()
	{
		telemetry.addData("Pickup State", pickupState.name());


		telemetry.update();
	}
}
