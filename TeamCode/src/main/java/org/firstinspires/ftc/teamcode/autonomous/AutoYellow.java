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
		robot.fixedClaw.close();

		if (!robot.distanceSystem.StatusOk())
		{
			telemetry.addLine("[ERROR] Distance sensor timeout!");
			telemetry.update();
		}
	}

	@Override
	protected void OnRun()
	{
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(180))
								.splineToConstantHeading(centimetersToInches(new Vector2d(-69, 20)), DegToRad(180), (pose2dDual, posePath, v) -> 65, (pose2dDual, posePath, v) -> new MinMax(-65, 65))
								.build(),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.CHAMBER_HIGH)
				)
		));

		Actions.runBlocking(RunSequentially(
				RunInParallel(
						RunSequentially(
								WaitFor(0.05),
								robot.drivetrain.actionBuilder(robot.drivetrain.pose)
										.lineToX(centimetersToInches(-72))
										.build()
						),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.CHAMBER_HIGH_PLACE)
				),
				WaitFor(0.1),
				robot.fixedClaw.openAction()
		));

		// Pick first yellow
		Actions.runBlocking(RunSequentially(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.splineToConstantHeading(centimetersToInches(new Vector2d(-52, -80)), - Math.PI / 2)
								.turnTo(Math.PI + 0.01)
								.build(),
						WaitFor(0.3),
						new InstantAction(() -> {
							robot.tumbler.setDestination(TumblerSystem.TumblerDestination.HOVER);
							robot.smallTumbler.setDestination(TumblerSystem.TumblerDestination.BUSY);
						}),
						WaitFor(0.75),
						new InstantAction(() -> robot.tumbler.setDestination(TumblerSystem.TumblerDestination.BUSY)),
						WaitFor(0.4),
						robot.claw.closeAction(),
						WaitFor(0.3),
						new InstantAction(() -> {
							robot.extendo.extend(ExtendoSystem.ExtendoLevel.RETRACTED);
							robot.tumbler.setDestination(TumblerSystem.TumblerDestination.TRANSFER);
							robot.smallTumbler.setDestination(TumblerSystem.TumblerDestination.IDLE);
						}),
						WaitFor(0.7),
						new InstantAction(() -> robot.scoreClaw.close()),
						WaitFor(0.3),
						new InstantAction(() -> robot.claw.open()),
						WaitFor(0.3),
						new InstantAction(() -> robot.tumbler.setDestination(TumblerSystem.TumblerDestination.IDLE))
				)
		);

		// Deliver first yellow
		Actions.runBlocking(RunSequentially(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.setTangent(0)
						.splineToLinearHeading(centimetersToInches(new Pose2d(-7, -96, 3 * Math.PI / 4)), 0)
						.build(),
				robot.lift.moveLiftTo(LiftSystem.LiftLevel.BASKET_HIGH),
				WaitFor(0.5),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				WaitFor(0.8),
				robot.scoreClaw.openAction(),
				WaitFor(0.7),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE),
				WaitFor(0.5),
				robot.lift.moveLiftTo(LiftSystem.LiftLevel.IDLE)
		));

		// Pickup second yellow
		Actions.runBlocking(RunSequentially(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.splineToLinearHeading(centimetersToInches(new Pose2d(-17
								, -105, Math.PI)), 0)
						.build(),
				robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.EXTENDED),
				WaitFor(0.3),
				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				WaitFor(0.75),
				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				WaitFor(0.4),
				robot.claw.closeAction(),
				WaitFor(0.3),
				robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.RETRACTED),
				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE),
				WaitFor(0.7),
				robot.scoreClaw.closeAction(),
				WaitFor(0.3),
				robot.claw.openAction(),
				WaitFor(0.3),
				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE)
		));

		// Deliver second yellow
		Actions.runBlocking(RunSequentially(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.setTangent(0)
						.splineToLinearHeading(centimetersToInches(new Pose2d(-8, -94, 3 * Math.PI / 18 * 3.75f)), 0)
						.build(),
				robot.lift.moveLiftTo(LiftSystem.LiftLevel.BASKET_HIGH),
				WaitFor(0.5),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				WaitFor(0.8),
				robot.scoreClaw.openAction(),
				WaitFor(0.7),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE),
				WaitFor(0.5),
				robot.lift.moveLiftTo(LiftSystem.LiftLevel.IDLE),
				WaitFor(5)
		));
	}
}