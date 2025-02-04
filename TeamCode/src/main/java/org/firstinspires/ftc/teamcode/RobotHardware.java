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
	public TumblerSystem tumbler, smallTumbler, scoreTumbler, scoreStumbler;
	public LiftSystem lift;
	public RotatorSystem rotator;
	public OpenCloseSystem claw, scoreClaw, scoreRotator, axle1, axle2;
	public DistanceSystem distanceSystem;

	public RobotHardware(HardwareMap hardwareMap)
	{
		drivetrain = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));

		extendo = new ExtendoSystem(hardwareMap.get(Servo.class, "extendo"));
		tumbler = new TumblerSystem(hardwareMap.get(Servo.class, "tumbler"), 0.525f, 0.525f, 0.525f, 0.525f, 0.49f);
		smallTumbler = new TumblerSystem(hardwareMap.get(Servo.class, "sTumbler"), 0.956f, 0.5f, 0.5f, 0.32f, 0.34f); 
		rotator = new RotatorSystem(hardwareMap.get(Servo.class, "rotator"));
		claw = new OpenCloseSystem(hardwareMap.get(Servo.class, "claw"), 0.19f, 0.57f);

		lift = new LiftSystem(hardwareMap.get(DcMotorEx.class, "lift1"), hardwareMap.get(DcMotorEx.class, "lift2"));
		scoreTumbler = new TumblerSystem(hardwareMap.get(Servo.class, "scoreTumbler"), 0.412f, 0.575f, 0.405f, 0, 0.877F);//hold is score position and pickup is wall
		scoreRotator = new  OpenCloseSystem(hardwareMap.get(Servo.class, "scoreRotator"), 0.758f, 0.06f);
		scoreStumbler = new TumblerSystem(hardwareMap.get(Servo.class, "scoreStumbler"), 0.926f, 0.926f, 0.823f, 0.632f, 0.671f); // hold is specimen scoring and hover is basket scoring pickup pos is wall
		scoreClaw = new OpenCloseSystem(hardwareMap.get(Servo.class, "scoreClaw"), 0.31f, 0.669f);
		axle1 = new OpenCloseSystem(hardwareMap.get(Servo.class, "axleServo1"), 0.5f, 0.39f);
		axle2 = new OpenCloseSystem(hardwareMap.get(Servo.class, "axleServo2"), 0.325f, 0.39f);

		distanceSystem = new DistanceSystem(hardwareMap.get(Rev2mDistanceSensor.class, "leftDist"),
											hardwareMap.get(Rev2mDistanceSensor.class, "rightDist"));
	}

	public void init()
	{
		extendo.init();
		tumbler.init();
		smallTumbler.setDestination(TumblerSystem.TumblerDestination.TRANSFER);
		rotator.init();
		claw.init();

		lift.init();
		scoreTumbler.init();
		scoreClaw.init();
		scoreStumbler.setDestination(TumblerSystem.TumblerDestination.TRANSFER);
		scoreRotator.init();

		axle1.init();
		axle2.init();
	}
}