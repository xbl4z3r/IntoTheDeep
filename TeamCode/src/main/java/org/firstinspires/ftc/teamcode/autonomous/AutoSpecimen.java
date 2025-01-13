package org.firstinspires.ftc.teamcode.autonomous;

import static org.firstinspires.ftc.teamcode.Utilities.DegToRad;
import static org.firstinspires.ftc.teamcode.Utilities.RunSequentially;
import static org.firstinspires.ftc.teamcode.Utilities.RunInParallel;
import static org.firstinspires.ftc.teamcode.Utilities.centimetersToInches;
import static org.firstinspires.ftc.teamcode.Utilities.WaitFor;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.BaseOpMode;
import org.firstinspires.ftc.teamcode.RobotHardware;
import org.firstinspires.ftc.teamcode.systems.ExtendoSystem;
import org.firstinspires.ftc.teamcode.systems.LiftSystem;
import org.firstinspires.ftc.teamcode.systems.TumblerSystem;

@Autonomous(name = "♿Auto♿ Specimen", group = "Auto")
public class AutoSpecimen extends BaseOpMode
{
	RobotHardware robot;

	@Override
	protected void OnInitialize()
	{
		robot = new RobotHardware(hardwareMap);
		robot.init();

		robot.fixedClaw.close();
	}

	@Override
	protected void OnStart()
	{
		// Deliver preload specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(180))
								.splineToConstantHeading(centimetersToInches(new Vector2d(-69, 3)), DegToRad(0))
								.build(),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.CHAMBER_HIGH)
				),
				robot.lift.moveLiftTo(LiftSystem.LiftLevel.CHAMBER_HIGH_PLACE),
				WaitFor(0.5),
				robot.fixedClaw.openAction(),
				WaitFor(0.1)
		));

		// Pick and deliver first sample
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(0))
								.splineToLinearHeading(centimetersToInches(new Pose2d(-30, 100, DegToRad(180))), DegToRad(90))
								.build(),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.IDLE)
				),

				robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.SAMPLE),
				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),

				WaitFor(0.5),

				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				WaitFor(0.15),
				robot.claw.closeAction(),
				WaitFor(0.1),

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
				WaitFor(0.4),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				WaitFor(0.4),
				robot.scoreClaw.openAction(),
				WaitFor(0.2)
		));

		// Pick and deliver second sample
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(90))//?
								.lineToYConstantHeading(centimetersToInches(125))
								.build(),
						robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE),
						robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.SAMPLE)
				),
				WaitFor(0.1), // ?

				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				WaitFor(0.15),
				robot.claw.closeAction(),
				WaitFor(0.1),

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
				WaitFor(0.4),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				WaitFor(0.4),
				robot.scoreClaw.openAction(),
				WaitFor(0.4),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE)
		));

		// Pick and deliver third sample
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.turnTo(DegToRad(160))
								.build(),
						robot.extendo.extendAction(ExtendoSystem.ExtendoLevel.SAMPLE),
						robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE),
						robot.rotator.setPositionAction(0.7)
				),
				WaitFor(0.1), // ?

				robot.tumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				robot.smallTumbler.setDestinationAction(TumblerSystem.TumblerDestination.BUSY),
				WaitFor(0.15),
				robot.claw.closeAction(),
				WaitFor(0.1),

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
				WaitFor(0.4),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.HOVER),
				WaitFor(0.4),
				robot.scoreClaw.openAction(),
				WaitFor(0.4),
				robot.scoreTumbler.setDestinationAction(TumblerSystem.TumblerDestination.IDLE)
		));

		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.build()
				),
				WaitFor(30)
		));
	}

	@Override
	protected void OnRun() {}
}