package org.firstinspires.ftc.teamcode.Testing;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.MecanumDrive;


@TeleOp(name = "Manual Drive Test", group = "Testing")
public final class ManualDriveTest extends LinearOpMode
{
	MecanumDrive mecanumDrive;
	Gamepad wheelsGamepad;

	@Override
	public void runOpMode() throws InterruptedException
	{
		Init();
		waitForStart();

		while (opModeIsActive() && !isStopRequested())
			Loop();
	}


	private void Init()
	{
		mecanumDrive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
		wheelsGamepad = gamepad1;
	}


	private void Loop()
	{
		Wheels();
		Telemetry();
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


	private void Telemetry()
	{
		telemetry.update();
	}
}