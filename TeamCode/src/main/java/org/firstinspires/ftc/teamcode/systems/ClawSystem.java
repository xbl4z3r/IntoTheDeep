package org.firstinspires.ftc.teamcode.systems;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.google.gson.annotations.Expose;
import com.qualcomm.robotcore.hardware.Servo;

public class ClawSystem extends AbstractSystem
{
	private static float CLAW_OPEN = 0;
	private static float CLAW_CLOSED = 1;

	@Expose
	private final Servo claw;

	public ClawSystem(Servo claw)
	{
		this(claw, CLAW_OPEN, CLAW_CLOSED);
	}

	public ClawSystem(Servo claw, float open, float closed)
	{
		this.claw = claw;
		CLAW_OPEN = open;
		CLAW_CLOSED = closed;
	}

	@Override
	public void init()
	{
		claw.setPosition(CLAW_OPEN);
	}

	public void open()
	{
		claw.setPosition(CLAW_OPEN);
	}

	public void close()
	{
		claw.setPosition(CLAW_CLOSED);
	}

	public Action openAction()
	{
		return new InstantAction(this::open);
	}

	public Action closeAction()
	{
		return new InstantAction(this::close);
	}
}
