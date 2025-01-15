package org.firstinspires.ftc.teamcode.systems;

import static org.firstinspires.ftc.teamcode.Utilities.RunSequentially;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

public class LiftSystem extends AbstractSystem
{
	// IDLE, BASKET_LOW, BASKET_HIGH, CLEAR_SPECIMEN, CHAMBER_HIGH, CHAMBER_HIGH_PLACE, SUSPEND
	private final int[] LIFT_LEVELS = { 30, 670, 1800, 200, 980, 620, 770 };
	public int TOLERANCE = 20; // Amount to subtract to place specimen on the CHAMBER_HIGH

	private final DcMotorEx lift1, lift2;

	public LiftSystem(DcMotorEx lift1, DcMotorEx lift2)
	{
		this.lift1 = lift1;
		this.lift2 = lift2;
		lift1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		lift1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		lift1.setDirection(DcMotorSimple.Direction.REVERSE);
		lift2.setDirection(DcMotorSimple.Direction.REVERSE);
	}

	@Override
	public void init()
	{
		lift1.setTargetPosition(0);
		lift2.setTargetPosition(0);
		lift1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
		lift1.setPower(1);
		lift2.setPower(1);
	}

	public void setLiftLevel(LiftLevel level)
	{
		lift1.setTargetPosition(LIFT_LEVELS[level.ordinal()]);
		lift2.setTargetPosition(LIFT_LEVELS[level.ordinal()]);
	}

	public void setLiftPower(double power)
	{
		lift1.setPower(power);
		lift2.setPower(power);
	}

	public Action moveLiftTo(LiftLevel level)
	{
		return RunSequentially(
				new InstantAction(() -> setLiftLevel(level)),
				telemetryPacket -> isLiftBusy()
		);
	}

	public boolean isLiftBusy()
	{
		return lift1.isBusy() || lift2.isBusy();
	}

	public enum LiftLevel
	{
		IDLE,
		BASKET_LOW,
		BASKET_HIGH,
		CLEAR_SPECIMEN,
		CHAMBER_HIGH,
		CHAMBER_HIGH_PLACE,
		SUSPEND
	}
}