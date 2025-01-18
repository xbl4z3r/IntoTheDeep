package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.systems.DistanceSystem;
import org.firstinspires.ftc.teamcode.systems.ExtendoSystem;
import org.firstinspires.ftc.teamcode.systems.LiftSystem;
import org.firstinspires.ftc.teamcode.systems.OpenCloseSystem;
import org.firstinspires.ftc.teamcode.systems.RotatorSystem;
import org.firstinspires.ftc.teamcode.systems.TumblerSystem;

public final class RobotHardware
{
	public final MecanumDrive drivetrain;
	public ExtendoSystem extendo;
	public TumblerSystem tumbler, smallTumbler, scoreTumbler;
	public LiftSystem lift;
	public RotatorSystem rotator;
	public OpenCloseSystem claw, scoreClaw, fixedClaw, axle1, axle2;
	public DistanceSystem distanceSystem;

	public RobotHardware(HardwareMap hardwareMap)
	{
		drivetrain = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));

		extendo = new ExtendoSystem(hardwareMap.get(Servo.class, "extendo1"), hardwareMap.get(Servo.class, "extendo2"));
		tumbler = new TumblerSystem(hardwareMap.get(Servo.class, "tumbler"), 0f, 0.145f, 0.74f, 0.82f);
		smallTumbler = new TumblerSystem(hardwareMap.get(Servo.class, "sTumbler"), 0.32f, 0.32f, 1f, 0.88f);
		rotator = new RotatorSystem(hardwareMap.get(Servo.class, "rotator"));
		claw = new OpenCloseSystem(hardwareMap.get(Servo.class, "claw"), 0.45f, 0.1f);

		lift = new LiftSystem(hardwareMap.get(DcMotorEx.class, "lift1"), hardwareMap.get(DcMotorEx.class, "lift2"));
		scoreTumbler = new TumblerSystem(hardwareMap.get(Servo.class, "scoreTumbler"), 0.09f, 0.09f, 0.9f, 0.8f);
		scoreClaw = new OpenCloseSystem(hardwareMap.get(Servo.class, "scoreClaw"), 0.8f, 0.6f);
		fixedClaw = new OpenCloseSystem(hardwareMap.get(Servo.class, "fixedClaw"), 0.75f, 0.4f);
		axle1 = new OpenCloseSystem(hardwareMap.get(Servo.class, "axleServo1"), 0.5f, 0.39f);
		axle2 = new OpenCloseSystem(hardwareMap.get(Servo.class, "axleServo2"), 0.325f, 0.39f);

		distanceSystem = new DistanceSystem(hardwareMap.get(Rev2mDistanceSensor.class, "leftDist"),
											hardwareMap.get(Rev2mDistanceSensor.class, "rightDist"));
	}

	public void init()
	{
		extendo.init();
		tumbler.init();
		smallTumbler.init();
		rotator.init();
		claw.init();

		lift.init();
		scoreTumbler.init();
		scoreClaw.init();
		fixedClaw.init();

		axle1.init();
		axle2.init();
	}
}