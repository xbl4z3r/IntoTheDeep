package org.firstinspires.ftc.teamcode.autonomous;

import static org.firstinspires.ftc.teamcode.Utilities.RunInParallel;
import static org.firstinspires.ftc.teamcode.Utilities.RunSequentially;
import static org.firstinspires.ftc.teamcode.Utilities.WaitFor;
import static org.firstinspires.ftc.teamcode.Utilities.centimetersToInches;

import com.acmerobotics.roadrunner.InstantAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.BaseOpMode;
import org.firstinspires.ftc.teamcode.RobotHardware;
import org.firstinspires.ftc.teamcode.teleop.DemoOp;

@Autonomous(name = "Yellow", group = "Auto")
public class Yellow extends BaseOpMode
{
	private RobotHardware robot;

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
	protected void OnRun()
	{
		robot.drivetrain.setStartPose(new Pose2d(0, 0, 0));

		// Deliver preloaded sample
		Actions.runBlocking(RunSequentially(
						RunInParallel(
								robot.drivetrain.actionBuilder(robot.drivetrain.pose)
										.setTangent(Math.PI)
										.splineToConstantHeading(centimetersToInches(new Vector2d(-78, -5)), Math.PI)
										.build(),
								new InstantAction(() -> {
									robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2]);
									robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2]);
								}),
								WaitFor(0.5),
								new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_CHAMBER))
						),
						new InstantAction(() -> {
							robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
							robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2] - DemoOp.Constants.LIFT_OFFSET);
						}),
						WaitFor(0.8),
						new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_OPEN)),
						WaitFor(0.3),
						new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_IDLE)),
						WaitFor(0.3)
				)
		);

		// Pick first yellow
		Actions.runBlocking(RunSequentially(
						RunInParallel(
								robot.drivetrain.actionBuilder(robot.drivetrain.pose)
										.splineToLinearHeading(centimetersToInches(new Pose2d(-78, -40, 1)), Math.PI)
										.build()
						)

				)
		);

		// Deliver first yellow
		Actions.runBlocking(RunSequentially(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
					//	.splineToLinearHeading(centimetersToInches(new Pose2d()))
						.build()
		));
	}
}