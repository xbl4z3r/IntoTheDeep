package org.firstinspires.ftc.teamcode.testing;

import static org.firstinspires.ftc.teamcode.Utilities.setTimeout;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Utilities;

@TeleOp(name = "Diver Test", group = "Testing")
public class DiverTest extends LinearOpMode
{
	Servo extendoServo1, extendoServo2, tumblerServo, smolTumbler, rotatorServo, clawServo;

	@Override
	public void runOpMode() throws InterruptedException
	{
		Init();
		waitForStart();
		while (!isStopRequested() && opModeIsActive())
			Run();
	}

	private void Init()
	{
		extendoServo1 = hardwareMap.get(Servo.class, "extendo1");
		extendoServo2 = hardwareMap.get(Servo.class, "extendo2");
		tumblerServo  = hardwareMap.get(Servo.class, "tumbler");
		smolTumbler   = hardwareMap.get(Servo.class, "sTumbler");
		rotatorServo  = hardwareMap.get(Servo.class, "rotator");
		clawServo     = hardwareMap.get(Servo.class, "claw");

		extendoServo1.setPosition(1);
		extendoServo2.setPosition(0);
		tumblerServo.setPosition(1);
		smolTumbler.setPosition(0.8);
		rotatorServo.setPosition(0.5);
		clawServo.setPosition(1);
	}

	private Utilities.State armState = Utilities.State.IDLE;
	private Utilities.State clawState = Utilities.State.IDLE;
	private int rotatorState = 1; // 0 left, 1 center, 2 right
	private boolean sf = false;
	private void Run()
	{
		// Rotator
		if (gamepad1.left_bumper && rotatorState > 0 && !sf)
		{
			sf = true;
			rotatorState--;
		}
		else if (gamepad1.right_bumper && rotatorState < 2 && !sf)
		{
			sf = true;
			rotatorState++;
		}
		if (!gamepad1.left_bumper && !gamepad1.right_bumper) sf = false;

		if (clawState == Utilities.State.IDLE)
		{
			switch (rotatorState)
			{
				case 0:
					rotatorServo.setPosition(0);
					break;
				case 1:
					rotatorServo.setPosition(0.5);
					break;
				case 2:
					rotatorServo.setPosition(1);
					break;
				default:
					break;
			}
		}

		// Grab
		if (gamepad1.x && armState == Utilities.State.IDLE)
		{
			armState = Utilities.State.BUSY;
			clawState = Utilities.State.IDLE;

			clawServo.setPosition(1);
			setTimeout(500,
					() -> {
						extendoServo1.setPosition(0);
						extendoServo2.setPosition(1);
						tumblerServo.setPosition(0.2);
						smolTumbler.setPosition(1);
					});
		}
		else if (gamepad1.a && armState == Utilities.State.BUSY)
		{
			armState = Utilities.State.IDLE;
			clawState = Utilities.State.BUSY;

			rotatorState = 1;

			clawServo.setPosition(0.2);
			setTimeout(500,
					() -> {
						extendoServo1.setPosition(1);
						extendoServo2.setPosition(0);
						tumblerServo.setPosition(1);
						smolTumbler.setPosition(0);
						rotatorServo.setPosition(0.5);
					});
		}


		Telemetry();
	}

	private void Telemetry()
	{
		telemetry.addData("Arm     State", armState.name());
		telemetry.addData("Rotator State", rotatorState);
		telemetry.addData("Claw    State", clawState.name());
		telemetry.update();
	}
}
