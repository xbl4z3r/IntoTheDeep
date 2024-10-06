package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.hardware.HardwareMap;

public final class RobotHardware
{
	private final HardwareMap hardwareMap;

	public final MecanumDrive mecanumDrive;

	public RobotHardware(HardwareMap _hardwareMap)
	{
		hardwareMap = _hardwareMap;

		mecanumDrive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
	}
}
