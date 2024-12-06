package org.firstinspires.ftc.teamcode.autonomous;

import static org.firstinspires.ftc.teamcode.Utilities.RunInParallel;
import static org.firstinspires.ftc.teamcode.Utilities.RunSequentially;
import static org.firstinspires.ftc.teamcode.Utilities.centimetersToInches;
import static org.firstinspires.ftc.teamcode.Utilities.setTimeout;

import com.acmerobotics.roadrunner.InstantAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SleepAction;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.BaseOpMode;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.teleop.DemoOp;


@Autonomous(name = "Autoboss MKII", group = "Auto")
public class AutobossMKII extends BaseOpMode
{
	MecanumDrive mecanumDrive;

	private Servo extendoServo, tumblerServo, smallTumbler, rotatorServo, clawServo, scoreTumbler, scoreClaw;
	private DcMotorEx lift1, lift2;

	Boolean specimenSide;

	@Override
	protected void OnInitialize()
	{
		while (specimenSide == null && opModeInInit() && !isStopRequested())
		{
			specimenSide = true;
			if (gamepad1.b || gamepad2.b)
				specimenSide = true;
			else if (gamepad1.x || gamepad2.x)
				specimenSide = false;
		}

		mecanumDrive = new MecanumDrive(hardwareMap);

		lift1 = hardwareMap.get(DcMotorEx.class, "lift1");
		lift2 = hardwareMap.get(DcMotorEx.class, "lift2");
		lift1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift1.setDirection(DcMotorSimple.Direction.REVERSE);
		lift1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift1.setTargetPosition(0);
		lift2.setTargetPosition(0);
		lift1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift1.setPower(1);
		lift2.setPower(1);

		extendoServo = hardwareMap.get(Servo.class, "extendo");
		tumblerServo = hardwareMap.get(Servo.class, "tumbler");
		smallTumbler = hardwareMap.get(Servo.class, "sTumbler");
		rotatorServo = hardwareMap.get(Servo.class, "rotator");
		clawServo = hardwareMap.get(Servo.class, "claw");
		scoreTumbler = hardwareMap.get(Servo.class, "scoreTumbler");
		scoreClaw = hardwareMap.get(Servo.class, "scoreClaw");

		extendoServo.setPosition(DemoOp.Constants.EXTENDO_RETRACTED);
		tumblerServo.setPosition(DemoOp.Constants.TUMBLER_IDLE);
		smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_BUSY);
		rotatorServo.setPosition(DemoOp.Constants.ROTATOR_BASE);
		clawServo.setPosition(DemoOp.Constants.CLAW_CLOSED);
		scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_IDLE);
		scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_CLOSED);
	}

	@Override
	protected void OnRun()
	{
		mecanumDrive.setStartPose(new Pose2d(0, 0, 0));

		// Deliver preloaded sample
		Actions.runBlocking(RunSequentially(
				RunInParallel(
					mecanumDrive.actionBuilder(mecanumDrive.pose)
							.setTangent(0)
							.lineToXConstantHeading(centimetersToInches(-69))
							.build(),
					new InstantAction(() -> {
						lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2]);
						lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2]);
						setTimeout(() -> {
						scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_BASKET);
						}, 500);
					})
				),
				new InstantAction(() -> {
					lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[3]);
					lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[3]);
					setTimeout(() -> {
								scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_OPEN);
								setTimeout(() -> scoreTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_IDLE), 300);
							},
							1000);
				}),
				new SleepAction(30000)
			)
		);
	}
}