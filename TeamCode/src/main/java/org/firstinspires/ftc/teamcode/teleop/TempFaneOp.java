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
	Gamepad wheelsGamepad, armGamepad;

	MecanumDrive mecanumDrive;

	DcMotorEx lift;
	Servo extendoServo1, extendoServo2, tumblerServo, smolTumbler, rotatorServo, clawServo;





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
		lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
		lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


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
			speed = 0.4f; // supress

		mecanumDrive.setDrivePowers(new PoseVelocity2d(new Vector2d(wheelsGamepad.left_stick_y, wheelsGamepad.left_stick_x).times(speed),
				wheelsGamepad.left_trigger - wheelsGamepad.right_trigger));
	}

	private Utilities.State armState = Utilities.State.IDLE;
	private Utilities.State clawState = Utilities.State.IDLE;
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
	}



	private void Lift()
	{
		lift.setPower(armGamepad.right_stick_y);
	}

	private void Telemetry()
	{
		telemetry.addData("Arm     State: ", armState.name());
		telemetry.addData("Rotator State: ", rotatorState);
		telemetry.addData("Claw    State: ", clawState.name());

		telemetry.addData("Lift Pow:      ", lift.getPower());
		telemetry.addData("Lift Pos:      ", lift.getCurrentPosition());

		telemetry.update();
	}
}
