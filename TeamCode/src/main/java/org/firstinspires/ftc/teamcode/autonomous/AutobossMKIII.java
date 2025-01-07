package org.firstinspires.ftc.teamcode.autonomous;

import static org.firstinspires.ftc.teamcode.Utilities.RunInParallel;
import static org.firstinspires.ftc.teamcode.Utilities.RunSequentially;
import static org.firstinspires.ftc.teamcode.Utilities.WaitFor;
import static org.firstinspires.ftc.teamcode.Utilities.centimetersToInches;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.BaseOpMode;
import org.firstinspires.ftc.teamcode.RobotHardware;
import org.firstinspires.ftc.teamcode.teleop.DemoOp;

@Autonomous(name = "Autoboss MKIII", group = "Auto")
public final class AutobossMKIII extends BaseOpMode
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
										.splineToConstantHeading(centimetersToInches(new Vector2d(-80, -9)), Math.PI)
										.build(),
								new InstantAction(() -> {
									robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2]);
									robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2]);
								}),
								new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_CHAMBER))
						),
						new InstantAction(() -> {
							robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
							robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
						}),
						WaitFor(0.4),
						new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_OPEN)),
						//WaitFor(0.3),
						new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_IDLE))
						//WaitFor(0.3)
				)
		);

		// Deliver samples
		Actions.runBlocking(RunSequentially(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						// First
						.setTangent(Math.PI / 4)
						.splineToConstantHeading(centimetersToInches(new Vector2d(-69, 68)), Math.PI)
						.setTangent(Math.PI)
						.splineToConstantHeading(centimetersToInches(new Vector2d(-125, 70)), Math.PI)
						.setTangent(Math.PI / 2)
						.splineToConstantHeading(centimetersToInches(new Vector2d(-25, 100)), 0)

						// Second
						.setTangent(0)
						.lineToXConstantHeading(centimetersToInches(-100))
						.splineToConstantHeading(centimetersToInches(new Vector2d(-125, 120)), Math.PI / 2)
						.setTangent(0)
						.splineToConstantHeading(centimetersToInches(new Vector2d(-25, 120)), 0)

						// Third
						//	.setTangent(0)
						//	.lineToXConstantHeading(centimetersToInches(-100))
						//	.splineToConstantHeading(centimetersToInches(new Vector2d(-125, 138)), Math.PI / 2)
						//	.setTangent(0)
						//	.splineToConstantHeading(centimetersToInches(new Vector2d(-25, 138)), 0)

						.build()
		));


		// Pick first specimen
		Actions.runBlocking(RunSequentially(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.setTangent(Math.PI)
						.splineToConstantHeading(centimetersToInches(new Vector2d(-28, 80)), Math.PI / 2)
						.build(),
				new InstantAction(() -> {
					//robot.extendoServo.setPosition(DemoOp.Constants.EXTENDO_EXTENDED);
					robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_WALL);
					robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_WALL);

				}),
				WaitFor(0.5),
				new InstantAction(() -> robot.clawServo.setPosition(DemoOp.Constants.CLAW_CLOSED)),
				WaitFor(0.5),
				new InstantAction(() -> {
					robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_IDLE);
					robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_IDLE);
				})


		));

		// Deliver first specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(Math.PI)
								.splineToLinearHeading(new Pose2d(centimetersToInches(-90), centimetersToInches(4), 0), Math.PI)
								.build(),
						RunSequentially(
								new InstantAction(() -> {
									robot.extendoServo.setPosition(DemoOp.Constants.EXTENDO_RETRACTED);
									robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_DELIVER);
									robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_IDLE);
								}),
								WaitFor(0.5),
								new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_CLOSED)),
								WaitFor(0.3),
								new InstantAction(() -> robot.clawServo.setPosition(DemoOp.Constants.CLAW_OPEN)),
								new InstantAction(() -> robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_WALL)),
								new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_CHAMBER)),
								WaitFor(1.5),
								new InstantAction(() -> {
									robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - 25);
									robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - 25);
								})
						)
				)
		));

		Actions.runBlocking(
				RunInParallel(
						RunSequentially(
								WaitFor(0.8),
								new InstantAction(() -> {
									robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
									robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
								}),
								WaitFor(0.4),
								new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_OPEN)),
								WaitFor(0.3),
								new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_IDLE))
						),
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.lineToX(centimetersToInches(-76))
								.build()
				)
		);

		// Pick SECOND specimen
		Actions.runBlocking(RunSequentially(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.splineToConstantHeading(centimetersToInches(new Vector2d(-28, 80)), 0)
						.build(),
				new InstantAction(() -> {
					//robot.extendoServo.setPosition(DemoOp.Constants.EXTENDO_EXTENDED);
					robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_WALL);
					robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_WALL);

				}),
				WaitFor(0.5),
				new InstantAction(() -> robot.clawServo.setPosition(DemoOp.Constants.CLAW_CLOSED)),
				WaitFor(0.5),
				new InstantAction(() -> {
					robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_IDLE);
					robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_IDLE);
				})
		));

		// Deliver second specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(Math.PI)
								.splineToLinearHeading(new Pose2d(centimetersToInches(-88), centimetersToInches(10), 0), Math.PI)
								.build(),
						RunSequentially(
								new InstantAction(() -> {
									robot.extendoServo.setPosition(DemoOp.Constants.EXTENDO_RETRACTED);
									robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_DELIVER);
									robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_IDLE);
								}),
								WaitFor(0.5),
								new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_CLOSED)),
								WaitFor(0.3),
								new InstantAction(() -> robot.clawServo.setPosition(DemoOp.Constants.CLAW_OPEN)),
								new InstantAction(() -> robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_WALL)),
								new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_CHAMBER)),
								WaitFor(1.5),
								new InstantAction(() -> {
									robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] + 50);
									robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] + 50);
								})
						)
				)
		));

		Actions.runBlocking(
				RunInParallel(
						RunSequentially(
								WaitFor(0.5),
								new InstantAction(() -> {
									robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
									robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
								}),
								WaitFor(0.8),
								new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_OPEN)),
								WaitFor(0.3),
								new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_IDLE))
						),
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.lineToX(centimetersToInches(-76))
								.build()
				)
		);

		// Park
		Actions.runBlocking(RunInParallel(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.setTangent(Math.PI / 2)
						.splineToLinearHeading(new Pose2d(centimetersToInches(-8), centimetersToInches(26), Math.PI / 2), Math.PI / 2)
						.build(),
				WaitFor(0.5),
				new InstantAction(() -> {
					robot.extendoServo.setPosition(DemoOp.Constants.EXTENDO_EXTENDED);
					robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_PICKUP);
				}),
				WaitFor(0.5)

		));
	}
}