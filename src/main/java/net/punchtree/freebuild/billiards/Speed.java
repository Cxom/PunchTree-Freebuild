package net.punchtree.freebuild.billiards;

class Speed {

    private double x, z;

    public Speed(double x, double z) {
        this.x = x;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getZ() {
        return z;
    }

    public void flipX() {
        this.x = -x;
    }

    public void flipZ() {
        this.z = -z;
    }

    public double getComponent(double theta) {
        return Math.cos(theta) * x + Math.sin(theta) * z;
    }

    public void addComponent(double theta, double speed) {
        x += Math.cos(theta) * speed;
        z += Math.sin(theta) * speed;
    }
}
