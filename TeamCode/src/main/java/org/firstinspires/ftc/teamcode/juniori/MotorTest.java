package org.firstinspires.ftc.teamcode.juniori;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

public class MotorTest extends LinearOpMode
{
	DcMotorEx motor;

	@Override
	public void runOpMode() throws InterruptedException
	{
		Init();
		waitForStart();
		Run();
	}

	private void Init()
	{
		motor = hardwareMap.get(DcMotorEx.class,"motor");
		motor.setDirection(DcMotorSimple.Direction.FORWARD);
		motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
	}

	private void Run()
	{
		while (opModeIsActive() && !isStopRequested())
		{
			motor.setPower(gamepad1.left_stick_y);

			Telemetry();
		}
	}

	private void Telemetry()
	{
		telemetry.addData("Motor Power: ", motor.getPower());

		telemetry.update();
	}
}

