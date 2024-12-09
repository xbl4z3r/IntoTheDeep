package org.firstinspires.ftc.teamcode.autonomous;

import static org.firstinspires.ftc.teamcode.Utilities.RunInParallel;
import static org.firstinspires.ftc.teamcode.Utilities.RunSequentially;
import static org.firstinspires.ftc.teamcode.Utilities.WaitFor;
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
import org.firstinspires.ftc.teamcode.RobotHardware;
import org.firstinspires.ftc.teamcode.teleop.DemoOp;

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
										.setTangent(0)
										.lineToXConstantHeading(centimetersToInches(-69))
										.build(),
								new InstantAction(() -> {
									robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2]);
									robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[2]);
								}),
								WaitFor(0.5),
								new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_BASKET))
						),
						new InstantAction(() -> {
							robot.lift1.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[3]);
							robot.lift2.setTargetPosition(DemoOp.Constants.LIFT_LEVELS[3]);
						}),
						WaitFor(1),
						new InstantAction(() -> robot.scoreClaw.setPosition(DemoOp.Constants.SCORE_CLAW_OPEN)),
						WaitFor(0.3f),
						new InstantAction(() -> robot.scoreTumbler.setPosition(DemoOp.Constants.SCORE_TUMBLER_IDLE))
				)
		);
	}
}