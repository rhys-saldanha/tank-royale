package net.robocode2.server.mappers;

import net.robocode2.json_schema.types.ScanField;
import net.robocode2.util.MathUtil;

public final class ScanFieldMapper {

	public static ScanField map(net.robocode2.model.ScanField scanField) {
		if (scanField == null) {
			return null;
		}
		ScanField mappedScanField = new ScanField();
		mappedScanField.setAngle(MathUtil.normalAbsoluteAngleDegrees(scanField.getAngle()));
		mappedScanField.setRadius(scanField.getRadius());
		return mappedScanField;
	}
}