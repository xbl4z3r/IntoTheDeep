package org.firstinspires.ftc.teamcode.systems;

import static org.firstinspires.ftc.teamcode.Utilities.RunSequentially;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.google.gson.annotations.Expose;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class LiftSystem extends AbstractSystem
{
	// IDLE, BASKET_LOW, BASKET_HIGH, CHAMBER_HIGH, CHAMBER_HIGH_PLACE, SUSPEND
	private static final int[] LIFT_LEVELS = {0, 670, 1800, 950, 700, 770};
	public static int TOLERANCE = 20; // Amount to subtract to place specimen on the CHAMBER_HIGH

	@Expose
	private final DcMotorEx lift;

	public LiftSystem(DcMotorEx lift)
	{
		this.lift = lift;
		lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
	}

	@Override
	public void init()
	{
		lift.setTargetPosition(0);
		lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift.setPower(1);
	}

	public void setLiftLevel(LiftLevel level)
	{
		lift.setTargetPosition(LIFT_LEVELS[level.ordinal()]);
	}

	public void setLiftPower(double power)
	{
		lift.setPower(power);
	}

	public Action moveLiftTo(LiftLevel level)
	{
		return RunSequentially(
				new InstantAction(() -> setLiftLevel(level)),
				telemetryPacket -> Math.abs(lift.getCurrentPosition() - lift.getTargetPosition()) > TOLERANCE
		);
	}

	public boolean isLiftBusy()
	{
		return lift.isBusy() || (Math.abs(lift.getCurrentPosition() - lift.getTargetPosition()) > lift.getTargetPositionTolerance());
	}

	public enum LiftLevel
	{
		IDLE,
		BASKET_LOW,
		BASKET_HIGH,
		CHAMBER_HIGH,
		CHAMBER_HIGH_PLACE,
		SUSPEND
	}
}
