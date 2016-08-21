// <applet code=BallDynam.class width=500 height=500>
// </applet>

package balldynam;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
//import com.bruceeckel.swing.*;

interface SharedConstants {
	int BALL_SIZE = 5;
	double STEP_SIZE = 0.1;
	double G = 6.67e+2;
	double DEFAULT_K = 30.;
	double NU = 0.01;
	double MIN_FRIC = 0.;
	double MAX_FRIC = 0.5;
	double DEFAULT_FRIC = 0.1;
	double FRIC_INT = 0.02;
	double DEFAULT_RO = 10.;
	double GRAV_ACCEL = 90.;
	String INTRO_TEXT = "Right click on the gray area to place a fixed " +
						"particle and left click to place a particle bonded " +
						"to the last one by a perfect spring. Click several times " +
						"to place several balls and drag the mouse with the button " +
						"clicked to place balls seperated by the spring's equilibrium " +
						"distance. Enable gravity " +
						"and friction by checking the check boxes above and " +
						"modify the gravity, friction constant, spring " +
						"coeficient and equilibrium distance values by pressing the Preferences button. " +
						"Press Start to begin simulation and Clear to clear the " +
						"screan. Note: fixed balls are black and non fixed ones are " +
						"blue.";
}

class BallInfo {
	double x, y;
	double vx, vy;
	double mass;
	int ball_num;
	boolean fixed_ball;
	BallInfo(double x,double y,double vx,double vy,double mass,int ball_num,boolean fixed_ball) {
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
		this.mass = mass;
		this.ball_num = ball_num;
		this.fixed_ball = fixed_ball;
	}
	BallInfo(int ball_num) {
		this(0.,0.,0.,0.,1.,ball_num,true);
	}
	BallInfo Add(double addx, double addy, double addvx, double addvy) {
		return new BallInfo(x+addx,y+addy,vx+addvx,vy+addvy,mass,ball_num,fixed_ball);
	}

	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public double getVx() {
		return vx;
	}
	public double getVy() {
		return vy;
	}
	public double getMass() {
		return mass;
	}
	public int getBallNum() {
		return ball_num;
	}
	public boolean getFixedBallMode() {
		return fixed_ball;
	}
	public void set(double x,double y,double vx,double vy,double mass) {
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
		this.mass = mass;
	}
	public void set(double x,double y,double vx,double vy) {
		this.set(x,y,vx,vy,this.mass);
	}

	public String toString() {
		return ("ball:"+ball_num+"x:"+this.getX()+"y:"+this.getY()+"vx:"+this.getVx()+"vy:"+this.getVy());
	}
}

class BallsClass extends Vector implements Runnable, SharedConstants {

	public Thread t;
	public boolean keep_running, ground_gravity, friction;
	public double fric_value, grav_value, k_value, ro_value;

	BallsClass() {
		super(10,10);
	}
	public void start() {
		t = new Thread(this,"Integration thread");
		t.start();
	}
	public void stop() {
		keep_running = false;
	}
	public void clear() {
		this.removeAllElements();
	}
	public void setGroundGravity(boolean ground_gravity) {
		this.ground_gravity = ground_gravity;
	}
	public void setFriction(boolean friction) {
		this.friction = friction;
	}
	public void setGravityValue(double grav_value) {
		this.grav_value = grav_value;
	}
	public void setFrictionValue(double fric_value) {
		this.fric_value = fric_value;
	}
	public void setKValue(double k_value) {
		this.k_value = k_value;
	}
	public void setRoValue(double ro_value) {
		this.ro_value = ro_value;
	}
	public boolean getGroundGravity() {
		return ground_gravity;
	}
	public boolean getFriction() {
		return friction;
	}
	public double getGravityValue() {
		return grav_value;
	}
	public double getFrictionValue() {
		return fric_value;
	}
	public double getKValue() {
		return k_value;
	}
	public double getRoValue() {
		return ro_value;
	}

	public double[] Function(double t, BallInfo ith_ball) {
		int num_balls = this.size();
		double k[] = new double[4];
		BallInfo jth_ball;
		double xi, yi, vxi, vyi;
		double xj, yj, vxj, vyj, massj;
		double power;
		xi = ith_ball.getX();
		yi = ith_ball.getY();
		vxi = ith_ball.getVx();
		vyi = ith_ball.getVy();
		k[0] = 0;
		k[1] = 0;
		int ball_num = ith_ball.getBallNum();
		/*for (int j=0;j<num_balls;j++) {*/

		for (int j=ball_num-1;j<=ball_num+1;j++) {
			if (j==ith_ball.getBallNum()) continue;
			try {
				jth_ball = (BallInfo)this.elementAt(j);
			} catch (ArrayIndexOutOfBoundsException e) {
				continue;
			}
			xj = jth_ball.getX();
			yj = jth_ball.getY();
 			vxj = jth_ball.getVx();
			vyj = jth_ball.getVy();
			massj = jth_ball.getMass();
/**** Uncomment for gravitational force
			power = Math.pow((xi-xj)*(xi-xj)+(yi-yj)*(yi-yj),1.5);
			k[2]+=(massj*(xi-xj)/power);
			k[3]+=(massj*(yi-yj)/power);
		}
		k[2]*=(-G);
		k[3]*=(-G);
****/
			power = Math.pow((xi-xj)*(xi-xj)+(yi-yj)*(yi-yj),0.5);
			try {
				k[2]+=-k_value*(xi-xj)*(1-ro_value/power);
				k[3]+=-k_value*(yi-yj)*(1-ro_value/power);
			}
			catch (ArithmeticException e) { }
		}
		if (ground_gravity) k[3]+=grav_value;
		if (friction) {
			k[2]+=-fric_value*vxi;
			k[3]+=-fric_value*vyi;
		}
		k[0]=vxi;
		k[1]=vyi;

		return k;
	}

	synchronized public void AdvanceStep(double t) {
		int num_balls = this.size();
		double k[][] = new double[4][];
		BallInfo ith_ball;

		for (int i=0;i<num_balls;i++) {
			ith_ball = (BallInfo)this.elementAt(i);
			//System.out.println(ith_ball);
			if (ith_ball.getFixedBallMode()) continue;
			k[0] = this.Function(t,ith_ball);
			k[1] = this.Function(t+STEP_SIZE/2,
						   ith_ball.Add(k[0][0]*STEP_SIZE/2,
						  		    k[0][1]*STEP_SIZE/2,
						  		    k[0][2]*STEP_SIZE/2,
						  		    k[0][3]*STEP_SIZE/2));
			k[2] = this.Function(t+STEP_SIZE/2,
						   ith_ball.Add(k[1][0]*STEP_SIZE/2,
						  		    k[1][1]*STEP_SIZE/2,
						  		    k[1][2]*STEP_SIZE/2,
						  		    k[1][3]*STEP_SIZE/2));
			k[3] = this.Function(t+STEP_SIZE,
						   ith_ball.Add(k[2][0]*STEP_SIZE,
						  		    k[2][1]*STEP_SIZE,
						  		    k[2][2]*STEP_SIZE,
						  		    k[2][3]*STEP_SIZE));
			double xi = ith_ball.getX();
			double yi = ith_ball.getY();
			double vxi = ith_ball.getVx();
			double vyi = ith_ball.getVy();
			double xi_new = xi +
					    STEP_SIZE/6*(k[0][0]+2*k[1][0]+2*k[2][0]+k[3][0]);
			double yi_new = yi +
					    STEP_SIZE/6*(k[0][1]+2*k[1][1]+2*k[2][1]+k[3][1]);
			double vxi_new = vxi +
					    STEP_SIZE/6*(k[0][2]+2*k[1][2]+2*k[2][2]+k[3][2]);
			double vyi_new = vyi +
					    STEP_SIZE/6*(k[0][3]+2*k[1][3]+2*k[2][3]+k[3][3]);
			ith_ball.set(xi_new,yi_new,vxi_new,vyi_new);

		}
	}

	public void run() {
		keep_running = true;
		while (keep_running) {
			try {
				t.sleep((int)(1000*STEP_SIZE));
			}
			catch (InterruptedException e) {};

			this.AdvanceStep(0.);
		}
	}
}

class PaintableBall extends JPanel implements SharedConstants {
	private int x, y;

	public PaintableBall(int x, int y) {
		this.x = x;
		this.y = y;
		this.setSize(BALL_SIZE,BALL_SIZE);
		this.setBackground(Color.lightGray);
		this.setLocation(new Point(x,y));
		this.setOpaque(false);
	}
	public void setPos(int x,int y) {
		this.x = x;
		this.y = y;
		this.setLocation(new Point(x,y));
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Dimension size = getSize();
		g.setColor(Color.blue);
		g.fillOval(0,0,BALL_SIZE,BALL_SIZE);
	}
}

class Ball extends JPanel implements Runnable, SharedConstants {
	private int x, y;
	private Thread t;
	private BallsClass ballsinfo;
	private BallInfo thisball;
	private boolean keep_running, threadSuspended;
	public Ball(int x, int y,BallsClass ballsinfo,BallInfo thisball) {
		t = new Thread(this, "Ball");
		this.x = x;
		this.y = y;
		this.ballsinfo = ballsinfo;
		this.thisball = thisball;
		this.setSize(BALL_SIZE,BALL_SIZE);
		this.setBackground(Color.lightGray);
		this.setLocation(new Point(x,y));
		this.setOpaque(false);
		t.start();
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Dimension size = getSize();
		if (thisball.getFixedBallMode()) {
			g.setColor(Color.black);
		} else {
			g.setColor(Color.blue);
		}
		g.fillOval(0,0,BALL_SIZE,BALL_SIZE);
	}
	synchronized public void clear() {
		notify();
		keep_running = false;
	}
	synchronized public void stop() {
		threadSuspended = true;
	}
	synchronized public void start() {
		threadSuspended = false;
		notify();
	}
	public void run() {
		keep_running = true;
		while (keep_running) {
			try {
				synchronized(this) {
					while (threadSuspended) wait();
				}
				//System.out.println("step:"+(int)(1000*STEP_SIZE));
				try {
					t.sleep((int)(1000*STEP_SIZE));
				}
				catch (InterruptedException e) {};
				x = (int)thisball.getX();
				y = (int)thisball.getY();
				this.setLocation(new Point(x,y));
				this.repaint((int)(STEP_SIZE*1000));
			} catch (InterruptedException e) {};
		}

	}
}

class MyDialog extends JDialog implements SharedConstants {
	BallsClass ballsinfo;

	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	JButton ok = new JButton("OK");
	JLabel fric_label = new JLabel("Friction: ",JLabel.RIGHT);
	JLabel grav_label = new JLabel("Gravity: ",JLabel.RIGHT);
	JLabel k_label = new JLabel("Spring coef: ",JLabel.RIGHT);
	JLabel ro_label = new JLabel("Equilibrium distance: ",JLabel.RIGHT);
	Box fric_text_box = new Box(BoxLayout.X_AXIS);
	Box grav_text_box = new Box(BoxLayout.X_AXIS);
	Box k_text_box = new Box(BoxLayout.X_AXIS);
	Box vb = new Box(BoxLayout.Y_AXIS);
	JTextField fric_text;
	JTextField grav_text;
	JTextField k_text;
	JTextField ro_text;

	JPanel ro_panel = new JPanel(null);
	PaintableBall ball1, ball2;

	MyDialog(BallsClass bi) {
		super((JFrame)null,"Preferences",true);
		ballsinfo = bi;

		ball1 = new PaintableBall(10,10);
		ball2 = new PaintableBall(10+(int)(ballsinfo.getRoValue()),10);

		fric_text = new JTextField(""+ballsinfo.getFrictionValue(),5);
		grav_text = new JTextField(""+ballsinfo.getGravityValue(),5);
		k_text = new JTextField(""+ballsinfo.getKValue(),5);
		ro_text = new JTextField(""+ballsinfo.getRoValue(),5);
		Container cp = getContentPane();
		cp.setLayout(gridbag);

		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				ballsinfo.setFrictionValue((Double.valueOf(fric_text.getText())).doubleValue());
				ballsinfo.setGravityValue((Double.valueOf(grav_text.getText())).doubleValue());
        		ballsinfo.setKValue((Double.valueOf(k_text.getText())).doubleValue());
				ballsinfo.setRoValue((Double.valueOf(ro_text.getText())).doubleValue());
				dispose(); // Closes the dialog
      		}
    	});
		ro_text.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ball2.setPos(10+(int)((Double.valueOf(ro_text.getText())).doubleValue()),10);
				ball2.repaint();
				ro_panel.repaint();
			}
		});
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(fric_label,c);
		cp.add(fric_label);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(fric_text,c);
		cp.add(fric_text);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(grav_label,c);
		cp.add(grav_label);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(grav_text,c);
		cp.add(grav_text);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(k_label,c);
		cp.add(k_label);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(k_text,c);
		cp.add(k_text);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(ro_label,c);
		cp.add(ro_label);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(ro_text,c);
		cp.add(ro_text);

		ro_panel.setBackground(Color.lightGray);
		//ro_panel.setBackground(Color.white);
		ro_panel.add(ball1);
		ro_panel.add(ball2);
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 20;
		gridbag.setConstraints(ro_panel,c);
		cp.add(ro_panel);
		ball1.repaint();
		ball2.repaint();

		c.fill = GridBagConstraints.NONE;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new Insets(20,0,0,0);
		gridbag.setConstraints(ok,c);
		cp.add(ok);

		setSize(200,200);
	}
}

public class BallDynam extends JApplet implements SharedConstants {
	private BallsClass ballsinfo = new BallsClass();
	private Vector balls = new Vector(10,10);

	private GridBagLayout gridbag = new GridBagLayout();
	private GridBagConstraints c = new GridBagConstraints();

	private JPanel pan = new JPanel(null);
	private JButton bstart = new JButton("Start");
	private JButton bclear = new JButton("Clear");
	private JButton bpref = new JButton("Preferences");
	/*private JCheckBox cbfb = new JCheckBox("Fixed ball");*/
	private JCheckBox cbgg = new JCheckBox("Ground gravity");
	private JCheckBox cbfr = new JCheckBox("Friction");
	private JTextArea intro = new JTextArea(INTRO_TEXT,20,15);

	static boolean started = false;
	static boolean fixed_balls_mode = false;

	int lastx, lasty;

	public void init() {
		Container cp = getContentPane();
		//Box bv = Box.createVerticalBox();
		//Box bh = Box.createHorizontalBox();
		//Container vc = new Container();
		cp.setLayout(gridbag);
		cp.setBackground(Color.white);

		pan.setBackground(Color.lightGray);
		//pan.setSize(500,500);
		pan.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
				e.consume();
				switch (e.getModifiers()) {
					case (MouseEvent.BUTTON1_MASK):
						fixed_balls_mode = false;
						break;
					case (MouseEvent.BUTTON3_MASK):
						fixed_balls_mode = true;
						break;
					default:
						return;
				}
            	int x = e.getX()-BALL_SIZE/2;
            	int y = e.getY()-BALL_SIZE/2;
				lastx = x;
				lasty = y;
				ballsinfo.addElement(new BallInfo(x,y,0,0,1.,
						   ballsinfo.size(),fixed_balls_mode));
            	Ball ball = new Ball(x,y,ballsinfo,
						   (BallInfo)ballsinfo.lastElement());
				balls.addElement(ball);
				pan.add(ball);
				ball.repaint();
            }
		});
		pan.addMouseMotionListener(new MouseMotionAdapter() {

			public void mouseDragged(MouseEvent e) {
				double ro = ballsinfo.getRoValue();
				e.consume();
				switch (e.getModifiers()) {
					case (MouseEvent.BUTTON1_MASK):
						fixed_balls_mode = false;
						break;
					case (MouseEvent.BUTTON3_MASK):
						fixed_balls_mode = true;
						break;
					default:
						return;
				}
				int x = e.getX()-BALL_SIZE/2;
            	int y = e.getY()-BALL_SIZE/2;
				double dist = Math.sqrt((x-lastx)*(x-lastx)+(y-lasty)*(y-lasty));
				if (dist >= ro) {
					int thisx = lastx + (int)((x-lastx)*ro/dist);
					int thisy = lasty + (int)((y-lasty)*ro/dist);
					ballsinfo.addElement(new BallInfo(thisx,thisy,0,0,1.,
						 				 ballsinfo.size(),fixed_balls_mode));
            		Ball ball = new Ball(thisx,thisy,ballsinfo,
					 			   (BallInfo)ballsinfo.lastElement());
					balls.addElement(ball);
					pan.add(ball);
					ball.repaint();
					lastx = thisx;
					lasty = thisy;
				}
			}
        });

		//bstart.setMargin(new Insets(5,10,5,10));
		bstart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!started) {
					ballsinfo.start();
					bstart.setText("Stop");
					started = true;
					for (int i=0;i<balls.size();i++) {
						((Ball)(balls.elementAt(i))).start();
					}
				}
				else {
					ballsinfo.stop();
					bstart.setText("Start");
					started = false;
					for (int i=0;i<balls.size();i++) {
						((Ball)(balls.elementAt(i))).stop();
					}
				}
			}
		});
		//bclear.setMargin(new Insets(5,10,5,10));
		bclear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				for (int i=0;i<balls.size();i++) {
					((Ball)(balls.elementAt(i))).clear();
				}
				boolean ground_gravity = ballsinfo.getGroundGravity();
				boolean friction = ballsinfo.getFriction();
				double fric_value = ballsinfo.getFrictionValue();
				double grav_value = ballsinfo.getGravityValue();
				double k_value = ballsinfo.getKValue();
				double ro_value = ballsinfo.getRoValue();
				ballsinfo.clear();
				ballsinfo = new BallsClass();
				ballsinfo.setGroundGravity(ground_gravity);
				ballsinfo.setFriction(friction);
				ballsinfo.setFrictionValue(fric_value);
				ballsinfo.setGravityValue(grav_value);
				ballsinfo.setKValue(k_value);
				ballsinfo.setRoValue(ro_value);
				balls.removeAllElements();
				pan.removeAll();
				pan.repaint();
				balls = new Vector(10,10);
				bstart.setText("Start");
				started = false;
			}
		});

		//bpref.setMargin(new Insets(5,10,5,10));
		bpref.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				MyDialog dlg = new MyDialog(ballsinfo);
				dlg.show();
			}
		});

		ballsinfo.setGroundGravity(false);
		cbgg.setBackground(Color.white);
		cbgg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ballsinfo.setGroundGravity(!ballsinfo.getGroundGravity());
			}
		});
		ballsinfo.setGravityValue(GRAV_ACCEL);

		ballsinfo.setFriction(false);
		cbfr.setBackground(Color.white);
		cbfr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ballsinfo.setFriction(!ballsinfo.getFriction());
			}
		});
		ballsinfo.setFrictionValue(DEFAULT_FRIC);

		ballsinfo.setKValue(DEFAULT_K);

		ballsinfo.setRoValue(DEFAULT_RO);
		/*bv.add(bstart);
		bv.add(bclear);
		bv.add(bpref);
		bv.add(cbfb);
		bv.add(cbgg);
		bv.add(cbfr);
		cp.add(pan);
		cp.add(Box.createHorizontalStrut(10));
		cp.add(bv);*/
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.BOTH;
		//c.ipadx = (int)(Integer.parseInt(getParameter("width"))*4./6.);
		//c.ipady = (int)(Integer.parseInt(getParameter("height")));
		gridbag.setConstraints(pan,c);
		cp.add(pan);
		c.ipadx = 0;
		c.ipady = 0;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;

		Container buttons = new Container();
		GridBagLayout gridbag2 = new GridBagLayout();
		GridBagConstraints c2 = new GridBagConstraints();
		buttons.setLayout(gridbag2);
		c2.gridwidth = GridBagConstraints.REMAINDER;
		gridbag2.setConstraints(bstart,c2);
		buttons.add(bstart);
		gridbag2.setConstraints(bclear,c2);
		buttons.add(bclear);
		gridbag2.setConstraints(bpref,c2);
		buttons.add(bpref);
		c2.anchor = GridBagConstraints.WEST;
		/*gridbag2.setConstraints(cbfb,c2);
		buttons.add(cbfb);*/
		gridbag2.setConstraints(cbgg,c2);
		buttons.add(cbgg);
		gridbag2.setConstraints(cbfr,c2);
		buttons.add(cbfr);
		intro.setEditable(false);
		intro.setLineWrap(true);
		intro.setWrapStyleWord(true);
		gridbag2.setConstraints(intro,c2);
		buttons.add(intro);
		c.weightx = 0.1;
		gridbag.setConstraints(buttons,c);
		cp.add(buttons);
	}

	public static void main(String[] args) {
            BallDynam ballDynam = new BallDynam();
	}
}
