package org.firstinspires.ftc.teamcode.systems;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.Servo;

public class ExtendoSystem extends AbstractSystem
{
	private final float RETRACTED = 0;
	private final float HALF = 0.5f;
	private final float SAMPLE = 0.78f;
	private final float THIRD_SAMPLE = 0.9f;
	private final float EXTENDED = 1;

	private ExtendoLevel _level = ExtendoLevel.RETRACTED;

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
		extendo1.setPosition(RETRACTED);
		extendo2.setPosition(RETRACTED);
	}

	public void extend(ExtendoLevel level)
	{
		_level = level;
		switch (level)
		{
			case RETRACTED:
				extendo1.setPosition(RETRACTED);
				extendo2.setPosition(RETRACTED);
				break;
			case HALF:
				extendo1.setPosition(HALF);
				extendo2.setPosition(HALF);
				break;
			case SAMPLE:
				extendo1.setPosition(SAMPLE);
				extendo2.setPosition(SAMPLE);
				break;
			case THIRD_SAMPLE:
				extendo1.setPosition(THIRD_SAMPLE);
				extendo2.setPosition(THIRD_SAMPLE);
				break;
			case EXTENDED:
				extendo1.setPosition(EXTENDED);
				extendo2.setPosition(EXTENDED);
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
		SAMPLE,
		THIRD_SAMPLE,
		EXTENDED
	}
}