package org.firstinspires.ftc.teamcode.testing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp(name = "EncoderRead", group = "Testing")
public class EncoderRead extends LinearOpMode
{
	DcMotorEx motor;

	@Override
	public void runOpMode() throws InterruptedException
	{
		motor = hardwareMap.get(DcMotorEx.class, "tumbler");
		motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

		waitForStart();

		while (opModeIsActive() && !isStopRequested())
		{
			motor.setPower(gamepad1.left_stick_y);

			telemetry.addData("Pose: ", motor.getCurrentPosition());
			telemetry.update();
		}
	}
}
