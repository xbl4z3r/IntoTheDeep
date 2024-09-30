package org.firstinspires.ftc.teamcode.Testing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "TwoMotorTest", group = "Testing")
public class TwoMotorTest extends LinearOpMode
{
	DcMotorEx motor1, motor2;

	@Override
	public void runOpMode() throws InterruptedException
	{
		motor1 = hardwareMap.get(DcMotorEx.class, "motor1");
		motor2 = hardwareMap.get(DcMotorEx.class, "motor2");
		motor1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
		motor2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
		motor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		motor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		motor1.setDirection(DcMotorSimple.Direction.REVERSE);

		waitForStart();

		while (!isStopRequested() && opModeIsActive())
		{
			motor1.setPower(gamepad1.right_stick_y);
			motor2.setPower(gamepad1.right_stick_y);

			telemetry.addData("Power: ", gamepad1.right_stick_y);
		}
	}
}
