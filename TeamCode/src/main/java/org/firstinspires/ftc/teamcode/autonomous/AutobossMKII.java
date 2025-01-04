package org.firstinspires.ftc.teamcode.autonomous;

import static org.firstinspires.ftc.teamcode.Utilities.RunInParallel;
import static org.firstinspires.ftc.teamcode.Utilities.RunSequentially;
import static org.firstinspires.ftc.teamcode.Utilities.WaitFor;
import static org.firstinspires.ftc.teamcode.Utilities.centimetersToInches;
import static org.firstinspires.ftc.teamcode.Utilities.setTimeout;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SleepAction;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.hardware.ams.AMSColorSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.BaseOpMode;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.RobotHardware;
import org.firstinspires.ftc.teamcode.teleop.DemoOp;
import org.opencv.core.Mat;

@Autonomous(name = "Autoboss MKII", group = "Auto")
public final class AutobossMKII extends BaseOpMode
{
	private RobotHardware robot;
	private Boolean specimenSide;

	@Override
	protected void OnInitialize()
	{
		robot = new RobotHardware(hardwareMap);
		robot.init();

		// Prevent preload from getting stuck
		robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_WALL / 4 * 3);

		// Catch the preload
		robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_CLOSED);
	}

	@Override
	protected void WhileWaitingForStart()
	{
		if (specimenSide == null) {
			if (input1.wasPressedThisFrame("b") || input2.wasPressedThisFrame("b"))
				specimenSide = true;
			else if (input1.wasPressedThisFrame("x") || input2.wasPressedThisFrame("x"))
				specimenSide = false;
		}
	}

	@Override
	protected void OnRun()
	{
		robot.drivetrain.setStartPose(new Pose2d(0, 0, 0));

		// Deliver preloaded sample
		Actions.runBlocking(RunSequentially(
						RunInParallel(
								robot.drivetrain.actionBuilder(robot.drivetrain.pose)
										.setTangent(Math.PI)
										.splineToLinearHeading(new Pose2d(-5.8, -37.5, Math.toRadians(135)), Math.toRadians(0))
										.build(),
								new InstantAction(() -> {
									robot.lift1.setTargetPosition(1700);
									robot.lift2.setTargetPosition(1700);
								}),
								new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_BASKET))
						),
						new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_OPEN)),
						WaitFor(0.3),
						new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_IDLE))
				)
		);
		//first sample
		Actions.runBlocking(RunSequentially(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(Math.PI)
								.splineToLinearHeading(new Pose2d(-10, -37, Math.toRadians(-193)), Math.toRadians(0))
								.build(),
						new InstantAction(() -> {
							robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
							robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
						}),
						new InstantAction(() -> {
							robot.extendoServo.setPosition(DemoOp.Constants.EXTENDO_EXTENDED);
						}),
						WaitFor(0.6),
						new InstantAction(() -> {
							robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_PICKUP);
							robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_BUSY);
							robot.clawServo.setPosition(DemoOp.Constants.CLAW_OPEN);
						}),
						PickupSample(),
						new InstantAction(() -> {
							robot.extendoServo.setPosition(DemoOp.Constants.EXTENDO_RETRACTED);
							robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_DELIVER + 0.03);
							robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_IDLE);
						}),
						WaitFor(0.5),
						new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_CLOSED)),
						WaitFor(0.12),
						new InstantAction(() -> robot.clawServo.setPosition(DemoOp.Constants.CLAW_OPEN)),
						WaitFor(0.2),
						new InstantAction(() -> robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_WALL)),

						RunInParallel(
								robot.drivetrain.actionBuilder(robot.drivetrain.pose)
										.setTangent(Math.PI)
										.splineToLinearHeading(new Pose2d(-5.8, -37.5, Math.toRadians(135)), Math.toRadians(0))
										.build(),
								new InstantAction(() -> {
									robot.lift1.setTargetPosition(1700);
									robot.lift2.setTargetPosition(1700);
								})

						),
						new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_BASKET)),
						WaitFor(0.4),
						new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_OPEN)),
						WaitFor(0.3),
						new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_IDLE))


				)
		);
		//second sample
		Actions.runBlocking(RunSequentially(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(Math.PI)

								.splineToLinearHeading(new Pose2d(-10, -36.9, Math.toRadians(-173.5)), Math.toRadians(0))
								.build(),
						new InstantAction(() -> {
							robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
							robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
						}),
						new InstantAction(() -> {
							robot.extendoServo.setPosition(DemoOp.Constants.EXTENDO_EXTENDED + 0.03);
						}),
						WaitFor(0.6),
						new InstantAction(() -> {
							robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_PICKUP);
							robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_BUSY);
							robot.clawServo.setPosition(DemoOp.Constants.CLAW_OPEN);
						}),
						PickupSample(),
						new InstantAction(() -> {
							robot.extendoServo.setPosition(DemoOp.Constants.EXTENDO_RETRACTED);
							robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_DELIVER + 0.03);
							robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_IDLE);
						}),
						WaitFor(0.5),
						new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_CLOSED)),
						WaitFor(0.12),
						new InstantAction(() -> robot.clawServo.setPosition(DemoOp.Constants.CLAW_OPEN)),
						WaitFor(0.2),
						new InstantAction(() -> robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_WALL)),

						RunInParallel(
								robot.drivetrain.actionBuilder(robot.drivetrain.pose)
										.setTangent(Math.PI)
										.splineToLinearHeading(new Pose2d(-5.8, -37.5, Math.toRadians(135)), Math.toRadians(0))
										.build(),
								new InstantAction(() -> {
									robot.lift1.setTargetPosition(1700);
									robot.lift2.setTargetPosition(1700);
								})
						),
						new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_BASKET)),
						WaitFor(0.4),
						new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_OPEN)),
						WaitFor(0.3),
						new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_IDLE))

				)
		);
		//third sample
		Actions.runBlocking(RunSequentially(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.setTangent(Math.PI)
						.splineToLinearHeading(new Pose2d(-10.5, -39, Math.toRadians(-156)), Math.toRadians(0))
						.build(),
				new InstantAction(() -> {
					robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
					robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
				}),
				new InstantAction(() -> {
					robot.extendoServo.setPosition(DemoOp.Constants.EXTENDO_EXTENDED);
				}),
				WaitFor(0.6),
				new InstantAction(() -> {
					robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_PICKUP);
					robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_BUSY);
					robot.clawServo.setPosition(DemoOp.Constants.CLAW_OPEN);
				}),
				PickupSample(),
				new InstantAction(() -> {
					robot.extendoServo.setPosition(DemoOp.Constants.EXTENDO_RETRACTED);
					robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_DELIVER + 0.03);
					robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_IDLE);
				}),
				WaitFor(0.5),
				new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_CLOSED)),
				WaitFor(0.12),
				new InstantAction(() -> robot.clawServo.setPosition(DemoOp.Constants.CLAW_OPEN)),
				WaitFor(0.2),
				new InstantAction(() -> robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_WALL)),

				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(Math.PI)
								.splineToLinearHeading(new Pose2d(-5.8, -37.5, Math.toRadians(135)), Math.toRadians(0))
								.build(),
						new InstantAction(() -> {
							robot.lift1.setTargetPosition(1700);
							robot.lift2.setTargetPosition(1700);
						})
				),
				new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_BASKET - 0.1)),
				WaitFor(0.6),
				new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_OPEN)),
				WaitFor(0.3),
				new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_IDLE))
		));
		//Parking
		Actions.runBlocking(RunSequentially(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.setTangent(Math.PI)
						.splineToLinearHeading(new Pose2d(-53, -3.5, Math.toRadians(90)), Math.toRadians(0))
						.build(),
				new InstantAction(() -> {
					robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
					robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
				})
		));

	}

	private Action PickupSample()
	{
		return RunSequentially(
				new InstantAction(() -> robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_PICKUP)),
				WaitFor(0.3),
				new InstantAction(() -> robot.clawServo.setPosition(DemoOp.Constants.CLAW_CLOSED)),
				WaitFor(0.3),
				new InstantAction(() -> robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_READY)),
				WaitFor(0.1)
		);
	}
}