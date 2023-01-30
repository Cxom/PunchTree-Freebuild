package net.punchtree.freebuild.util.armorstand;

import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

@Deprecated
public class ArmorStandUtils {

    public static void resetPose(ArmorStand stand) {
        stand.setBodyPose(new EulerAngle(0, 0, 0));
        stand.setLeftArmPose(new EulerAngle(0, 0, 0));
        stand.setRightArmPose(new EulerAngle(0, 0, 0));
        stand.setLeftLegPose(new EulerAngle(0, 0, 0));
        stand.setRightLegPose(new EulerAngle(0, 0, 0));
    }

}
