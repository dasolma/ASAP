package dasolma.com.asaplib.drawing.opengl.actions;

import dasolma.com.asaplib.drawing.opengl.OpenGLDrawing;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dasolma on 22/12/14.
 */
public class DrawRectangle implements IOpenglAction {

    private int x;
    private int y;
    private int w;
    private int h;
    private float g;
    private float r;
    private float b;

    public DrawRectangle(int x, int y, int w, int h, float r, float g, float b) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.r = r;
        this. g = g;
        this. b = b;
    }

    @Override
    public void run(GL10 gl) {

        gl.glPushMatrix();
        gl.glTranslatef(x, y, 0);
        gl.glScalef(w, h, 0);
        gl.glColor4f(r, g, b, 1);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, OpenGLDrawing.vertexBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        gl.glPopMatrix();

    }

/*
    //gl.glPushMatrix();
    gl.glTranslatef(x, y, 0);
    gl.glColor4f(r, g, b, 1);


    //gl.glScalef(w, h, 0);

    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, OpenGLDrawing.vertexBuffer);
    //gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
    gl.glDrawArrays(GL10.GL_POINTS, 0, 12);
    gl.glEnable(GL10.GL_POINT_SIZE);
    gl.glPointSize(w);

    //gl.glPopMatrix();
    */
}
