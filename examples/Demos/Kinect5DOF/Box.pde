public class Box {
  Scene scene;
  public InteractiveFrame iFrame;
  float w, h, d;
  int c;

  public Box(Scene scn, InteractiveFrame iF) {
    scene = scn;
    iFrame = iF;
    iFrame.setGrabsInputThreshold(25);
    setSize();
    setColor();
  }

  public Box(Scene scn) {
    scene = scn;
    iFrame = new InteractiveFrame(scn);
    iFrame.setGrabsInputThreshold(25);
    setSize();
    setColor();    
    setPosition();
  }

  public void draw() {
    draw(false);
  }

  public void draw(boolean drawAxes) {
    scene.pg().pushMatrix();
    iFrame.applyWorldTransformation();

    if (drawAxes)
      //DrawingUtils.drawAxes(parent, PApplet.max(w,h,d)*1.3f);
      scene.drawAxes(PApplet.max(w, h, d)*1.3f);
    scene.pg().noStroke();
    if (scene.grabsAnyAgentInput(iFrame))
      scene.pg().fill(255, 0, 0);
    else
      scene.pg().fill(getColor());
    //Draw a box    
    scene.pg().box(w, h, d);

    scene.pg().popMatrix();
  }

  public void setSize() {
    w = scene.pApplet().random(10, 40);
    h = scene.pApplet().random(10, 40);
    d = scene.pApplet().random(10, 40);
  }

  public void setSize(float myW, float myH, float myD) {
    w=myW; 
    h=myH; 
    d=myD;
  }  

  public int getColor() {
    return c;
  }

  public void setColor() {
    c = color(scene.pApplet().random(0, 255), scene.pApplet().random(0, 255), scene.pApplet().random(0, 255));
  }

  public void setColor(int myC) {
    c = myC;
  }

  public Vec getPosition() {
    return iFrame.position();
  }  

  public void setPosition() {
    float low = -100;
    float high = 100;
    iFrame.setPosition(new Vec(scene.pApplet().random(low, high), scene.pApplet().random(low, high), scene.pApplet().random(low, high)));
  }

  public void setPosition(Vec pos) {
    iFrame.setPosition(pos);
  }

  public Quat getOrientation() {
    return (Quat)iFrame.orientation();
  }

  public void setOrientation(Vec v) {
    Vec to = Vec.subtract(v, iFrame.position()); 
    iFrame.setOrientation(new Quat(new Vec(0, 1, 0), to));
  }
}