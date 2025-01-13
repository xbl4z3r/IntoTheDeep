package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.systems.OpenCloseSystem;
import org.firstinspires.ftc.teamcode.systems.ExtendoSystem;
import org.firstinspires.ftc.teamcode.systems.LiftSystem;
import org.firstinspires.ftc.teamcode.systems.RotatorSystem;
import org.firstinspires.ftc.teamcode.systems.TumblerSystem;
import org.firstinspires.ftc.teamcode.teleop.FaneOp;

public final class RobotHardware
{
	public final MecanumDrive drivetrain;
	public ExtendoSystem extendo;
	public TumblerSystem tumbler, smallTumbler, scoreTumbler;
	public LiftSystem lift;
	public RotatorSystem rotator;
	public OpenCloseSystem claw, scoreClaw, fixedClaw, axle1, axle2;

	public RobotHardware(HardwareMap hardwareMap)
	{
		smallTumbler = new TumblerSystem(hardwareMap.get(Servo.class, "sTumbler"), 0.32f, 0.32f, 1f, 0.88f, true);
		scoreTumbler = new TumblerSystem(hardwareMap.get(Servo.class, "scoreTumbler"), 0.09f, 0.09f, 0.8f, 0.8f);
		drivetrain = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
		scoreClaw = new OpenCloseSystem(hardwareMap.get(Servo.class, "scoreClaw"), 0.8f, 0.6f);
		fixedClaw = new OpenCloseSystem(hardwareMap.get(Servo.class, "fixedClaw"), 0.75f, 0.45f);
		extendo = new ExtendoSystem(hardwareMap.get(Servo.class, "extendo1"), hardwareMap.get(Servo.class, "extendo2"));
		tumbler = new TumblerSystem(hardwareMap.get(Servo.class, "tumbler"), 0f, 0.145f, 0.74f, 0.82f);
		rotator = new RotatorSystem(hardwareMap.get(Servo.class, "rotator"));
		axle1 = new OpenCloseSystem(hardwareMap.get(Servo.class, "axleServo1"), 0.5f, 0.39f);
		axle2 = new OpenCloseSystem(hardwareMap.get(Servo.class, "axleServo2"), 0.325f, 0.39f);
		claw = new OpenCloseSystem(hardwareMap.get(Servo.class, "claw"), 0.45f, 0.1f);
		lift = new LiftSystem(hardwareMap.get(DcMotorEx.class, "lift"));
	}

	public void init()
	{
		smallTumbler.init();
		scoreClaw.init();
		fixedClaw.init();
		extendo.init();
		tumbler.init();
		rotator.init();
		claw.init();
		lift.init();
	}
}
