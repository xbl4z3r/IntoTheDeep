package org.firstinspires.ftc.teamcode.testing;

import com.acmerobotics.roadrunner.Math;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Utilities;

@TeleOp(name = "Servo Pos Test", group = "Testing")
public final class ServoPosTest extends LinearOpMode
{
	Servo servo;

	@Override
	public void runOpMode() throws InterruptedException
	{
		double pos = 0.5;
		servo = hardwareMap.get(Servo.class, "servo");
		servo.setPosition(pos);

		telemetry.setMsTransmissionInterval(50);

		waitForStart();

		while (!isStopRequested())
		{
			pos += gamepad1.right_stick_x * 0.0005;
			pos = Math.clamp(pos, 0.0, 1.0);

			servo.setPosition(pos);

			telemetry.addData("Servo Pos: ", pos);
			telemetry.update();
		}
	}
}