package org.firstinspires.ftc.teamcode.systems;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.google.gson.annotations.Expose;
import com.qualcomm.robotcore.hardware.Servo;

public class ExtendoSystem extends AbstractSystem
{
	private static final float EXTENDO_RETRACTED = 0;
	private static final float EXTENDO_EXTENDED = 1;

	private ExtendoLevel _level = ExtendoLevel.RETRACTED;

	@Expose
	private final Servo extendo1, extendo2;

	public ExtendoSystem(Servo extendo1, Servo extendo2)
	{
		this.extendo1 = extendo1;
		this.extendo2 = extendo2;
		extendo2.setDirection(Servo.Direction.REVERSE);
	}

	@Override
	public void init()
	{
		_level = ExtendoLevel.RETRACTED;
		extendo1.setPosition(EXTENDO_RETRACTED);
		extendo2.setPosition(EXTENDO_RETRACTED);
	}

	public void extend(ExtendoLevel level)
	{
		_level = level;
		switch (level)
		{
			case RETRACTED:
				extendo1.setPosition(EXTENDO_RETRACTED);
				extendo2.setPosition(EXTENDO_RETRACTED);
				break;
			case HALF:
				extendo1.setPosition(EXTENDO_EXTENDED / 2);
				extendo2.setPosition(EXTENDO_EXTENDED / 2);
				break;
			case EXTENDED:
				extendo1.setPosition(EXTENDO_EXTENDED);
				extendo2.setPosition(EXTENDO_EXTENDED);
				break;
		}
	}

	public Action extendAction(ExtendoLevel level)
	{
		return new InstantAction(() -> extend(level));
	}

	public ExtendoLevel getExtendoLevel()
	{
		return _level;
	}

	public enum ExtendoLevel
	{
		RETRACTED,
		HALF,
		EXTENDED
	}
}
