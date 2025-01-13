package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.systems.ExtendoSystem;
import org.firstinspires.ftc.teamcode.systems.TumblerSystem;
import org.firstinspires.ftc.teamcode.teleop.FaneOp;

public final class RobotHardware
{
	public final MecanumDrive drivetrain;
	public final DcMotorEx lift;
	public Servo smallTumbler, rotatorServo, clawServo, scoreTumbler, scoreClaw, fixedClaw, axleServo1, axleServo2;

	public ExtendoSystem extendo;
	public TumblerSystem tumbler;

	public RobotHardware(HardwareMap hardwareMap)
	{
		drivetrain = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
		extendo = new ExtendoSystem(hardwareMap);
		tumbler = new TumblerSystem(hardwareMap);

		lift = hardwareMap.get(DcMotorEx.class, "lift");

		smallTumbler = hardwareMap.get(Servo.class, "sTumbler");
		rotatorServo = hardwareMap.get(Servo.class, "rotator");
		clawServo = hardwareMap.get(Servo.class, "claw");
		scoreTumbler = hardwareMap.get(Servo.class, "scoreTumbler");
		scoreClaw = hardwareMap.get(Servo.class, "scoreClaw");
		fixedClaw = hardwareMap.get(Servo.class, "fixedClaw");
		axleServo1 = hardwareMap.get(Servo.class, "axleServo1");
		axleServo2 = hardwareMap.get(Servo.class, "axleServo2");

		lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

		smallTumbler.setDirection(Servo.Direction.REVERSE);
	}

	public void init()
	{
		lift.setTargetPosition(0);
		lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift.setPower(1);


		smallTumbler.setPosition(FaneOp.Constants.SMALL_TUMBLER_DELIVER);
		rotatorServo.setPosition(FaneOp.Constants.ROTATOR_BASE);
		clawServo.setPosition(FaneOp.Constants.CLAW_OPEN);
		scoreTumbler.setPosition(FaneOp.Constants.SCORE_TUMBLER_IDLE);
		scoreClaw.setPosition(FaneOp.Constants.SCORE_CLAW_OPEN);
		fixedClaw.setPosition(FaneOp.Constants.FIXED_CLAW_OPEN);
		axleServo1.setPosition(FaneOp.Constants.AXLE_SERVO1_UNLOCKED);
		axleServo2.setPosition(FaneOp.Constants.AXLE_SERVO2_UNLOCKED);
	}
}
