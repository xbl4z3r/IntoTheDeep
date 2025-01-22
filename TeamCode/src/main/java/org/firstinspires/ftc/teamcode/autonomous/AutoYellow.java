package org.firstinspires.ftc.teamcode.autonomous;

import static org.firstinspires.ftc.teamcode.Utilities.DegToRad;
import static org.firstinspires.ftc.teamcode.Utilities.RunInParallel;
import static org.firstinspires.ftc.teamcode.Utilities.RunSequentially;
import static org.firstinspires.ftc.teamcode.Utilities.WaitFor;
import static org.firstinspires.ftc.teamcode.Utilities.centimetersToInches;

import com.acmerobotics.roadrunner.InstantAction;
import com.acmerobotics.roadrunner.MinMax;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.BaseOpMode;
import org.firstinspires.ftc.teamcode.RobotHardware;
import org.firstinspires.ftc.teamcode.systems.ExtendoSystem;
import org.firstinspires.ftc.teamcode.systems.LiftSystem;
import org.firstinspires.ftc.teamcode.systems.RotatorSystem;
import org.firstinspires.ftc.teamcode.systems.TumblerSystem;

@Autonomous(name = "♿Auto♿ Yellow", group = "Auto")
public class AutoYellow extends BaseOpMode
{
	private RobotHardware robot;

	@Override
	protected void OnInitialize()
	{
		robot = new RobotHardware(hardwareMap);
		robot.init();
		robot.scoreClaw.close();
		robot.lift.setLiftPower(1);
		robot.drivetrain.setStartPose(new Pose2d(0, 0, DegToRad(-90)));

		if (!robot.distanceSystem.StatusOk()) {
			telemetry.addLine("[ERROR] Distance sensor timeout!");
			telemetry.update();
		}
	}

	@Override
	protected void OnRun()
	{
		// Deliver preload
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(0))
								.splineToLinearHeading(centimetersToInches(new Pose2d(28, 95, DegToRad(-45))), DegToRad(45))
								.build(),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.BASKET_HIGH),
						robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY)
				),
				robot.scoreClaw.openAction(),
				WaitFor(0.1)
		));

		// Drive to pick up first sample
		Actions.runBlocking(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.splineToConstantHeading(centimetersToInches(new Vector2d(27, 85)), DegToRad(0))
						.afterTime(0.1, robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER))
						.afterTime(0.2, robot.lift.moveLiftTo(LiftSystem.LiftLevel.IDLE))
						.afterTime(0.1, robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER))
						.afterTime(0.3, robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER))
						.build()
		);

		// Pick up first sample
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.turnTo(0)
								.build(),
						robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.EXTENDED)
				),
				RunInParallel(
						robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
						robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY)
				),
				WaitFor(0.2),
				robot.claw.closeAction(),
				WaitFor(0.2),
				RunInParallel(
						robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER),
						robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER)
				)
		));

		// Drive to drop first sample
		Actions.runBlocking(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.splineToLinearHeading(centimetersToInches(new Pose2d(28, 95, DegToRad(-45))), DegToRad(-45))
						.afterTime(0.1, robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.RETRACTED))
						.afterTime(1, robot.scoreClaw.closeAction())
						.afterTime(1.2, robot.claw.openAction())
						.afterTime(1.4, robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE))
						.afterTime(1.4, robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE))
						.build()
		);

		// Drop first sample
		Actions.runBlocking(RunSequentially(
				robot.lift.moveLiftTo(LiftSystem.LiftLevel.BASKET_HIGH),
				WaitFor(0.5),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				WaitFor(0.4),
				robot.scoreClaw.openAction(),
				WaitFor(0.3),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER)
		));

		// Drive to pickup second sample
		Actions.runBlocking(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.turnTo(0)
						.afterTime(0, robot.lift.moveLiftTo(LiftSystem.LiftLevel.IDLE))
						.splineToConstantHeading(centimetersToInches(new Vector2d(25, 107)), DegToRad(0))
						.afterTime(0.1, robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER))
						.afterTime(0.1, robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER))
						.afterTime(0.3, robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER))
						.build()
		);

		// Pick up second sample
		Actions.runBlocking(RunInParallel(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.turnTo(0)
						.build(),
				RunSequentially(
						robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.EXTENDED),
						WaitFor(0.4),
						RunInParallel(
								robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
								robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY)
						),
						WaitFor(0.2),
						robot.claw.closeAction(),
						WaitFor(0.2),
						RunInParallel(
								robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER),
								robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER),
								robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.RETRACTED)
						)
				)
		));

		// Drive to drop second sample
		Actions.runBlocking(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.splineToLinearHeading(centimetersToInches(new Pose2d(28, 95, DegToRad(-45))), DegToRad(-45))
						.afterTime(0.5, robot.scoreClaw.closeAction())
						.afterTime(0.6, robot.claw.openAction())
						.afterTime(0.7, robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE))
						.afterTime(0.7, robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE))
						.build()
		);

		// Drop second sample
		Actions.runBlocking(RunSequentially(
				robot.lift.moveLiftTo(LiftSystem.LiftLevel.BASKET_HIGH),
				WaitFor(0.5),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				WaitFor(0.4),
				robot.scoreClaw.openAction(),
				WaitFor(0.3),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER)
		));

		// Drive to pick up third sample
		Actions.runBlocking(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.turnTo(0)
						.afterTime(0, robot.lift.moveLiftTo(LiftSystem.LiftLevel.IDLE))
						.splineToLinearHeading(centimetersToInches(new Pose2d(25, 109, DegToRad(20))), DegToRad(20))
						.afterTime(0.1, robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER))
						.afterTime(0.1, robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER))
						.afterTime(0.3, robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER))
						.build()
		);

		// Pick up third sample
		Actions.runBlocking(RunInParallel(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.turnTo(DegToRad(20))
						.build(),
				RunSequentially(
						robot.rotator.setPositionAction(0.35),
						robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.EXTENDED),
						WaitFor(0.4),
						RunInParallel(
								robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
								robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY)
						),
						WaitFor(0.2),
						robot.claw.closeAction(),
						WaitFor(0.2),
						RunInParallel(
								robot.rotator.setPositionAction(RotatorSystem.ROTATOR_BASE),
								robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER),
								robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER),
								robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.RETRACTED)
						)
				)
		));

		// Drive to drop third sample
		Actions.runBlocking(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.splineToLinearHeading(centimetersToInches(new Pose2d(28, 95, DegToRad(-45))), DegToRad(-45))
						.afterTime(0.5, robot.scoreClaw.closeAction())
						.afterTime(0.6, robot.claw.openAction())
						.afterTime(0.7, robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE))
						.afterTime(0.7, robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE))
						.build()
		);

		// Drop third sample
		Actions.runBlocking(RunSequentially(
				robot.lift.moveLiftTo(LiftSystem.LiftLevel.BASKET_HIGH),
				WaitFor(0.5),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				WaitFor(0.5),
				robot.scoreClaw.openAction(),
				WaitFor(0.3),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER)
		));

		// Drive to park
		Actions.runBlocking(RunSequentially(
				robot.lift.moveLiftTo(LiftSystem.LiftLevel.IDLE),
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.splineToLinearHeading(centimetersToInches(new Pose2d(130, 10, DegToRad(90))), DegToRad(-90))
						.afterTime(0, robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER))
						.afterTime(0.2, robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY))
						.lineToY(centimetersToInches(5))
						.build(),
				WaitFor(2) // Prevent premature exit
		));
	}
}