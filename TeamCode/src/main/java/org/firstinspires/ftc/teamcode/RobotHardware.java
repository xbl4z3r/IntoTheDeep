package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.systems.ClawSystem;
import org.firstinspires.ftc.teamcode.systems.ExtendoSystem;
import org.firstinspires.ftc.teamcode.systems.LiftSystem;
import org.firstinspires.ftc.teamcode.systems.RotatorSystem;
import org.firstinspires.ftc.teamcode.systems.TumblerSystem;
import org.firstinspires.ftc.teamcode.teleop.FaneOp;

public final class RobotHardware
{
	public final MecanumDrive drivetrain;
	public Servo smallTumbler, scoreTumbler, axleServo1, axleServo2;

	public ExtendoSystem extendo;
	public TumblerSystem tumbler;
	public LiftSystem lift;
	public RotatorSystem rotator;
	public ClawSystem claw, scoreClaw, fixedClaw;

	public RobotHardware(HardwareMap hardwareMap)
	{
		drivetrain = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
		scoreClaw = new ClawSystem(hardwareMap.get(Servo.class, "scoreClaw"), 0.8f, 0.6f);
		fixedClaw = new ClawSystem(hardwareMap.get(Servo.class, "fixedClaw"), 0.75f, 0.45f);
		extendo = new ExtendoSystem(hardwareMap.get(Servo.class, "extendo1"), hardwareMap.get(Servo.class, "extendo2"));
		tumbler = new TumblerSystem(hardwareMap.get(Servo.class, "tumbler"));
		rotator = new RotatorSystem(hardwareMap.get(Servo.class, "rotator"));
		claw = new ClawSystem(hardwareMap.get(Servo.class, "claw"), 0.45f, 0.1f);
		lift = new LiftSystem(hardwareMap.get(DcMotorEx.class, "lift"));

		smallTumbler = hardwareMap.get(Servo.class, "sTumbler");
		scoreTumbler = hardwareMap.get(Servo.class, "scoreTumbler");
		axleServo1 = hardwareMap.get(Servo.class, "axleServo1");
		axleServo2 = hardwareMap.get(Servo.class, "axleServo2");

		smallTumbler.setDirection(Servo.Direction.REVERSE);
	}

	public void init()
	{
		scoreClaw.init();
		fixedClaw.init();
		extendo.init();
		tumbler.init();
		rotator.init();
		claw.init();
		lift.init();

		smallTumbler.setPosition(FaneOp.Constants.SMALL_TUMBLER_DELIVER);
		scoreTumbler.setPosition(FaneOp.Constants.SCORE_TUMBLER_IDLE);
		axleServo1.setPosition(FaneOp.Constants.AXLE_SERVO1_UNLOCKED);
		axleServo2.setPosition(FaneOp.Constants.AXLE_SERVO2_UNLOCKED);
	}
}
