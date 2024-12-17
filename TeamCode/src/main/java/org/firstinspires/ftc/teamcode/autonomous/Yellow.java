package org.firstinspires.ftc.teamcode.autonomous;

import static org.firstinspires.ftc.teamcode.Utilities.RunInParallel;
import static org.firstinspires.ftc.teamcode.Utilities.RunSequentially;
import static org.firstinspires.ftc.teamcode.Utilities.WaitFor;
import static org.firstinspires.ftc.teamcode.Utilities.centimetersToInches;

import com.acmerobotics.roadrunner.InstantAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SleepAction;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.Const;
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
										.splineToConstantHeading(centimetersToInches(new Vector2d(-78, 20)), Math.PI)
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
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.splineToConstantHeading(centimetersToInches(new Vector2d(-46, -82)), -Math.PI / 2)
								.turnTo(Math.PI)
								.build(),
						WaitFor(0.3),
						new InstantAction(() -> {
							robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_READY);
							robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_BUSY);
						}),
						WaitFor(0.5),
						new InstantAction(() -> robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_PICKUP)),
						WaitFor(0.4),
						new InstantAction(() -> robot.clawServo.setPosition(DemoOp.Constants.CLAW_CLOSED)),
						WaitFor(0.3),
						new InstantAction(() -> {
							robot.extendoServo.setPosition(DemoOp.Constants.EXTENDO_RETRACTED);
							robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_DELIVER);
							robot.smallTumbler.setPosition(DemoOp.Constants.SMALL_TUMBLER_IDLE);
						}),
						WaitFor(0.7),
						new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_CLOSED)),
						WaitFor(0.3),
						new InstantAction(() -> robot.clawServo.setPosition(DemoOp.Constants.CLAW_OPEN)),
						WaitFor(0.3),
						new InstantAction(() -> robot.tumblerServo.setPosition(DemoOp.Constants.TUMBLER_IDLE))
				)
		);

		// Deliver first yellow
		Actions.runBlocking(RunSequentially(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.setTangent(-Math.PI / 2)
						.splineToLinearHeading(centimetersToInches(new Pose2d(-20, -90, 3 * Math.PI / 4)), -Math.PI / 2)
						.build(),
				new InstantAction(() -> {
					robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[1]);
					robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[1]);
				}),
				WaitFor(0.3),
				new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_BASKET)),
				WaitFor(0.4),
				new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_OPEN)),
				WaitFor(1)
		));


	//	Actions.runBlocking(new SleepAction(10000));
	}
}