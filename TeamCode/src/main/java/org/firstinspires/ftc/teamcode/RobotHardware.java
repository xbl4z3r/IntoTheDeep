package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.teleop.DemoOp;

public final class RobotHardware
{
	private final HardwareMap hardwareMap;

	public final MecanumDrive drivetrain;
	public final DcMotorEx lift1, lift2;
	public Servo extendoServo, tumblerServo, smallTumbler, rotatorServo, clawServo, scoreTumbler, scoreClaw;

	public RobotHardware(HardwareMap _hardwareMap)
	{
		hardwareMap = _hardwareMap;

		drivetrain = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));

		lift1 = hardwareMap.get(DcMotorEx.class, "lift1");
		lift2 = hardwareMap.get(DcMotorEx.class, "lift2");
		extendoServo = hardwareMap.get(Servo.class, "extendo");
		tumblerServo = hardwareMap.get(Servo.class, "tumbler");
		smallTumbler = hardwareMap.get(Servo.class, "sTumbler");
		rotatorServo = hardwareMap.get(Servo.class, "rotator");
		clawServo = hardwareMap.get(Servo.class, "claw");
		scoreTumbler = hardwareMap.get(Servo.class, "scoreTumbler");
		scoreClaw = hardwareMap.get(Servo.class, "scoreClaw");

		lift1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift1.setDirection(DcMotorSimple.Direction.REVERSE);
		lift1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
		lift2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

		smallTumbler.setDirection(Servo.Direction.REVERSE);
	}

	public void init()
	{
		lift1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift1.setTargetPosition(0);
		lift2.setTargetPosition(0);
		lift1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift1.setPower(1);
		lift2.setPower(1);

		extendoServo.setPosition(DemoOp.Constants.EXTENDO_RETRACTED);
		tumblerServo.setPosition(DemoOp.Constants.TUMBLER_IDLE);
		smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_IDLE);
		rotatorServo.setPosition(DemoOp.Constants.ROTATOR_BASE);
		clawServo.setPosition(DemoOp.Constants.CLAW_OPEN);
		scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_IDLE);
		scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_OPEN);
	}
}
