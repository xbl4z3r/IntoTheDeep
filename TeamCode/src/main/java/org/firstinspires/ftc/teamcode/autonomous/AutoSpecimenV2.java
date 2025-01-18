package org.firstinspires.ftc.teamcode.autonomous;

import static org.firstinspires.ftc.teamcode.Utilities.DegToRad;
import static org.firstinspires.ftc.teamcode.Utilities.RunInParallel;
import static org.firstinspires.ftc.teamcode.Utilities.RunSequentially;
import static org.firstinspires.ftc.teamcode.Utilities.WaitFor;
import static org.firstinspires.ftc.teamcode.Utilities.centimetersToInches;

import com.acmerobotics.roadrunner.MinMax;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.BaseOpMode;
import org.firstinspires.ftc.teamcode.RobotHardware;
import org.firstinspires.ftc.teamcode.systems.LiftSystem;

@Autonomous(name = "♿Auto♿ Specimen V2", group = "Auto")
public class AutoSpecimenV2 extends BaseOpMode
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
		// Deliver preload specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(180))
								.splineToConstantHeading(centimetersToInches(new Vector2d(-68, 10)), DegToRad(180), (pose2dDual, posePath, v) -> 65, (pose2dDual, posePath, v) -> new MinMax(-65, 65))
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
				WaitFor(0.1),
				robot.fixedClaw.openAction()
		));

		// Move samples to human and grab first specimen
		Actions.runBlocking(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(45))
								.splineToConstantHeading(centimetersToInches(new Vector2d(-60, 70)), DegToRad(90), (pose2dDual, posePath, v) -> 150, (pose2dDual, posePath, v) -> new MinMax(-150, 150))
								.setTangent(DegToRad(180))
								.splineToConstantHeading(centimetersToInches(new Vector2d(-115, 95)), DegToRad(45), (pose2dDual, posePath, v) -> 150, (pose2dDual, posePath, v) -> new MinMax(-150, 150))
								.setTangent(0)
								.lineToX(centimetersToInches(-25))
								.setTangent(DegToRad(180))
								.splineToConstantHeading(centimetersToInches(new Vector2d(-115, 120)), DegToRad(45), (pose2dDual, posePath, v) -> 150, (pose2dDual, posePath, v) -> new MinMax(-150, 150))
								.setTangent(0)
								.lineToX(centimetersToInches(-25))
								.setTangent(DegToRad(180))
								.splineToConstantHeading(centimetersToInches(new Vector2d(-115, 140)), DegToRad(45), (pose2dDual, posePath, v) -> 150, (pose2dDual, posePath, v) -> new MinMax(-150, 150))
								.setTangent(0)
								.lineToX(centimetersToInches(-25))
								.setTangent(DegToRad(270))
								.splineToLinearHeading(centimetersToInches(new Pose2d(-5, 70, DegToRad(180))), DegToRad(0))
								.setTangent(DegToRad(0))
								.lineToX(centimetersToInches(6))
								.afterTime(0.1, robot.fixedClaw.closeAction())
								.afterTime(0.5, robot.lift.moveLiftTo(LiftSystem.LiftLevel.CLEAR_SPECIMEN))
								.build(),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.ZERO)
				)
		);

		// Drive to place first specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(180))
								.strafeToLinearHeading(centimetersToInches(new Vector2d(-50, 0)), DegToRad(0))
								.setTangent(DegToRad(180))
								.lineToX(centimetersToInches(-73))
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
										.lineToX(centimetersToInches(-77))
										.build()
						),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.CHAMBER_HIGH_PLACE)
				),
				WaitFor(0.15),
				robot.fixedClaw.openAction(),
				WaitFor(0.1)
		));

		// Grab second specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(0))
								.splineToLinearHeading(centimetersToInches(new Pose2d(-7, 75, DegToRad(180))), DegToRad(0))
								.setTangent(DegToRad(0))
								.lineToX(centimetersToInches(3))
								.afterTime(0.2, robot.fixedClaw.closeAction())
								.afterTime(0.4, robot.lift.moveLiftTo(LiftSystem.LiftLevel.CLEAR_SPECIMEN))
								.build(),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.ZERO)
				)
		));

		// Drive to place second specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(180))
								.strafeToLinearHeading(centimetersToInches(new Vector2d(-50, -5)), DegToRad(0))
								.setTangent(DegToRad(180))
								.lineToX(centimetersToInches(-84))
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
										.lineToX(centimetersToInches(-89))
										.build()
						),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.CHAMBER_HIGH_PLACE)
				),
				WaitFor(0.15),
				robot.fixedClaw.openAction(),
				WaitFor(0.1)
		));

		// Grab third specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(0))
								.splineToLinearHeading(centimetersToInches(new Pose2d(-7, 80, DegToRad(180))), DegToRad(0))
								.setTangent(DegToRad(0))
								.lineToX(centimetersToInches(0))
								.afterTime(0.2, robot.fixedClaw.closeAction())
								.afterTime(0.4, robot.lift.moveLiftTo(LiftSystem.LiftLevel.CLEAR_SPECIMEN))
								.build(),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.ZERO)
				)
		));

		// Drive to place third specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						robot.drivetrain.actionBuilder(robot.drivetrain.pose)
								.setTangent(DegToRad(180))
								.strafeToLinearHeading(centimetersToInches(new Vector2d(-50, -10)), DegToRad(0))
								.setTangent(DegToRad(180))
								.lineToX(centimetersToInches(-87))
								.build(),
						RunSequentially(
								WaitFor(0.1),
								robot.lift.moveLiftTo(LiftSystem.LiftLevel.CHAMBER_HIGH)
						)
				)
		));

		// Place third specimen
		Actions.runBlocking(RunSequentially(
				RunInParallel(
						RunSequentially(
								WaitFor(0.05),
								robot.drivetrain.actionBuilder(robot.drivetrain.pose)
										.lineToX(centimetersToInches(-95))
										.build()
						),
						robot.lift.moveLiftTo(LiftSystem.LiftLevel.CHAMBER_HIGH_PLACE)
				),
				WaitFor(0.15),
				robot.fixedClaw.openAction(),
				WaitFor(0.1),
				robot.lift.moveLiftTo(LiftSystem.LiftLevel.ZERO)
		));
	}
}