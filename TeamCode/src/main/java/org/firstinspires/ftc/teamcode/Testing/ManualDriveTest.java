package org.firstinspires.ftc.teamcode.Testing;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.MecanumDrive;


@TeleOp(name = "Manual Drive Test", group = "aTesting")
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
		mecanumDrive.setDrivePowers(new PoseVelocity2d(new Vector2d(wheelsGamepad.left_stick_x, wheelsGamepad.left_stick_y),
												wheelsGamepad.left_trigger - wheelsGamepad.right_trigger));
	}


	private void Telemetry()
	{
		telemetry.addData("Front Left", mecanumDrive.leftFront.getCurrent(CurrentUnit.MILLIAMPS));
		telemetry.addData("Front Right", mecanumDrive.rightFront.getCurrent(CurrentUnit.MILLIAMPS));
		telemetry.addData("Back Left", mecanumDrive.leftBack.getCurrent(CurrentUnit.MILLIAMPS));
		telemetry.addData("Back Right", mecanumDrive.rightBack.getCurrent(CurrentUnit.MILLIAMPS));

		telemetry.update();
	}
}