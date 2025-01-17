package org.firstinspires.ftc.teamcode.autonomous;

import static org.firstinspires.ftc.teamcode.Utilities.DegToRad;
import static org.firstinspires.ftc.teamcode.Utilities.RunSequentially;
import static org.firstinspires.ftc.teamcode.Utilities.RunInParallel;
import static org.firstinspires.ftc.teamcode.Utilities.centimetersToInches;
import static org.firstinspires.ftc.teamcode.Utilities.WaitFor;

import com.acmerobotics.roadrunner.Action;
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

@Autonomous(name = "♿Auto♿ Specimen", group = "Auto")
public class AutoSpecimen extends BaseOpMode
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
	protected void OnStart()
	{
		// Deliver preload specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(180))
								.splineToConstantHeading(centimetersToInches(new Vector2d(-68, 5)), DegToRad(180), (pose2dDual, posePath, v) -> 60, (pose2dDual, posePath, v) -> new MinMax(-40, 60))
								.build(),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.CHAMBER_HIGH)
				)
		));

		Actions.runBlocking(RunSequentially(
				RunInParallel(
						RunSequentially(
								WaitFor(0.05),
								robot.drivetrain.actionBuilder(robot.drivetrain.pose)
										.lineToX(centimetersToInches(-71))
										.build()
						),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.CHAMBER_HIGH_PLACE)
				),
				WaitFor(0.2),
				robot.fixedClaw.openAction(),
				WaitFor(0.1)
		));

		// Pick and deliver first sample
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(0))
								.splineToLinearHeading(centimetersToInches(new Pose2d(-30, 100, DegToRad(177))), DegToRad(90))
								.build(),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.IDLE)
				),

				robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.SAMPLE),
				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),

				WaitFor(0.5),

				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				WaitFor(0.2),
				robot.claw.closeAction(),
				WaitFor(0.2),

				robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.RETRACTED),
				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER),

				WaitFor(0.5),
				robot.scoreClaw.closeAction(),
				WaitFor(0.1),
				robot.claw.openAction(),
				WaitFor(0.1),
				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				WaitFor(0.3),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				WaitFor(0.4),
				robot.scoreClaw.openAction(),
				WaitFor(0.1)
		));

		// Pick and deliver second sample
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(90))
								.lineToYConstantHeading(centimetersToInches(125))
								.build(),
						robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE),
						robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.SAMPLE)
				),

				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				WaitFor(0.2),
				robot.claw.closeAction(),
				WaitFor(0.2),

				robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.RETRACTED),
				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER),

				WaitFor(0.4),
				robot.scoreClaw.closeAction(),
				WaitFor(0.1),
				robot.claw.openAction(),
				WaitFor(0.1),
				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				WaitFor(0.3),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				WaitFor(0.4),
				robot.scoreClaw.openAction(),
				WaitFor(0.1)
		));

		// Pick and deliver third sample
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.turnTo(DegToRad(153))
								.build(),
						robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE),
						robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.THIRD_SAMPLE),
						robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE),
						robot.rotator.setPositionAction(0.55)
				),

				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				WaitFor(0.2),
				robot.claw.closeAction(),
				WaitFor(0.2),

				robot.rotator.setPositionAction(RotatorSystem.ROTATOR_BASE),
				robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.RETRACTED),
				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.TRANSFER),

				WaitFor(0.5),
				robot.scoreClaw.closeAction(),
				WaitFor(0.1),
				robot.claw.openAction(),
				WaitFor(0.1),
				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				WaitFor(0.3),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				WaitFor(0.4),
				robot.scoreClaw.openAction(),
				WaitFor(0.4),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE),
				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE),
				WaitFor(0.1)
		));

		// Retrieve first specimen
		Actions.runBlocking(RunSequentially(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.setTangent(DegToRad(270))
						.splineToLinearHeading(centimetersToInches(new Pose2d(-15, 80, DegToRad(180))), DegToRad(0))
						.build(),
				WaitFor(0.1)
		));

		// Pickup first specimen
		Actions.runBlocking(RunSequentially(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.lineToX(1)
						.build(),
				WaitFor(0.1),
				robot.fixedClaw.closeAction(),
				WaitFor(0.2),
				robot.lift.moveLiftTo(LiftSystem.LiftLevel.CLEAR_SPECIMEN)
		));

		// Drive to place first specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(180))
								.splineToLinearHeading(centimetersToInches(new Pose2d(-50, 0, DegToRad(0))), DegToRad(180))
								.splineToConstantHeading(centimetersToInches(new Vector2d( -75, 0)), DegToRad(180))
								.build(),
						RunSequentially(
								WaitFor(0.1),
								robot.lift.moveLiftTo(LiftSystem.LiftLevel.CHAMBER_HIGH)
						)
				)
		));

		// Place first specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						RunSequentially(
								WaitFor(0.05),
								robot.drivetrain.actionBuilder(robot.drivetrain.pose)
										.lineToX(centimetersToInches(-78))
										.build()
						),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.CHAMBER_HIGH_PLACE)
				),
				WaitFor(0.2),
				robot.fixedClaw.openAction(),
				WaitFor(0.1)
		));

		// Retrieve second specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(0))
								.splineToLinearHeading(centimetersToInches(new Pose2d(-15, 80, DegToRad(180))), DegToRad(0))
								.build(),
						RunSequentially(
								WaitFor(0.1),
								robot.lift.moveLiftTo(LiftSystem.LiftLevel.IDLE)
						)
				),
				WaitFor(0.1)
		));

		// Pickup the second specimen
		Actions.runBlocking(RunSequentially(
				robot.drivetrain.actionBuilder(robot.drivetrain.pose)
						.lineToX(1)
						.build(),
				WaitFor(0.1),
				robot.fixedClaw.closeAction(),
				WaitFor(0.2),
				robot.lift.moveLiftTo(LiftSystem.LiftLevel.CLEAR_SPECIMEN)
		));

		// Drive to place second specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(180))
								.splineToLinearHeading(centimetersToInches(new Pose2d(-50, -10, DegToRad(0))), DegToRad(180))
								.splineToConstantHeading(centimetersToInches(new Vector2d( -77, -10)), DegToRad(180))
								.build(),
						RunSequentially(
								WaitFor(0.1),
								robot.lift.moveLiftTo(LiftSystem.LiftLevel.CHAMBER_HIGH)
						)
				)
		));

		// Place second specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						RunSequentially(
								WaitFor(0.05),
								robot.drivetrain.actionBuilder(robot.drivetrain.pose)
										.lineToX(centimetersToInches(-83))
										.build()
						),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.CHAMBER_HIGH_PLACE)
				),
				WaitFor(0.2),
				robot.fixedClaw.openAction(),
				WaitFor(10)
		));
	}

	@Override
	protected void OnRun()
	{
	}
}