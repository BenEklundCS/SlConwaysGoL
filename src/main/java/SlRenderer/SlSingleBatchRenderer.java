package SlRenderer;

import CSC133.SlWindow;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniform3f;

// put render stuff here

public class SlSingleBatchRenderer {
    private static final int OGL_MATRIX_SIZE = 16;
    private final Matrix4f viewProjMatrix = new Matrix4f();
    private final FloatBuffer myFloatBuffer = BufferUtils.createFloatBuffer(OGL_MATRIX_SIZE);
    private int vpMatLocation = 0;
    private final long window;

    private final int WIN_WIDTH = SlWindow.getWinWidth();
    private final int WIN_HEIGHT = SlWindow.getWinHeight();

    public SlSingleBatchRenderer(long window) {
        this.window = window;
    }

    public void render() {
        try {
            renderLoop();
        } finally {
            glfwTerminate();
            Objects.requireNonNull(glfwSetErrorCallback(null)).free();
        }
    } // public void render()

    void renderLoop() {
        glfwPollEvents();
        initOpenGL();
        renderObjects();
        /* Process window messages in the main thread */
        while (!glfwWindowShouldClose(window)) {
            glfwWaitEvents();
        }
    } // void renderLoop()
    void initOpenGL() {

        final float BG_RED = 0.0f;
        final float BG_GREEN = 0.0f;
        final float BG_BLUE = 1.0f;
        final float BG_ALPHA = 1.0f;

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glViewport(0, 0, WIN_WIDTH, WIN_HEIGHT);

        glClearColor(BG_RED, BG_GREEN, BG_BLUE, BG_ALPHA); // background color

        // call glCreateProgram() here - we have no gl-context here
        int shader_program = glCreateProgram();
        int vs = glCreateShader(GL_VERTEX_SHADER);

        glShaderSource(vs,
                "uniform mat4 viewProjMatrix;" +
                        "void main(void) {" +
                        " gl_Position = viewProjMatrix * gl_Vertex;" +
                        "}");

        glCompileShader(vs);
        glAttachShader(shader_program, vs);
        int fs = glCreateShader(GL_FRAGMENT_SHADER);

        glShaderSource(fs,
                "uniform vec3 color;" +
                        "void main(void) {" +
                        // This guy sets the shape color :)
                        " gl_FragColor = vec4(1.0f, 0.1f, 0.6f, 1.0f);" + // gl_FragColor = vec4(0.7f, 0.5f, 0.1f, 1.0f);"
                        "}");

        glCompileShader(fs);
        glAttachShader(shader_program, fs);
        glLinkProgram(shader_program);
        glUseProgram(shader_program);
        vpMatLocation = glGetUniformLocation(shader_program, "viewProjMatrix");
        return;
    } // void initOpenGL()
    void renderObjects() {

        while (!glfwWindowShouldClose(window)) {

            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            int vbo = glGenBuffers();
            int ibo = glGenBuffers();

            int rows = 150;
            int cols = 150;

            SlGridOfSquares grid = new SlGridOfSquares(rows, cols);
            float[] vertices = grid.getVertices();
            int[] indices = grid.getIndices();
            //System.out.println(Arrays.toString(vertices));
            //System.out.println(Arrays.toString(indices));
            //System.out.println(CSC133.slWindow.getWinWidth());
            //float[] vertices = {-20f, -20f, 20f, -20f, 20f, 20f, -20f, 20f};
            //int[] indices = {0, 1, 2, 0, 2, 3};

            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, (FloatBuffer) BufferUtils.
                    createFloatBuffer(vertices.length).
                    put(vertices).flip(), GL_STATIC_DRAW);
            glEnableClientState(GL_VERTEX_ARRAY);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, (IntBuffer) BufferUtils.
                    createIntBuffer(indices.length).
                    put(indices).flip(), GL_STATIC_DRAW);

            final int LEFT = 0;
            final int RIGHT = 200;
            final int BOTTOM = 0;
            final int TOP = 200;
            final int ZNEAR = 0;
            final int ZFAR = 10;
            final int SIZE = 2;

            glVertexPointer(SIZE, GL_FLOAT, 0, 0L);
            viewProjMatrix.setOrtho(LEFT, RIGHT, BOTTOM, TOP, ZNEAR, ZFAR);
            glUniformMatrix4fv(vpMatLocation, false,
                    viewProjMatrix.get(myFloatBuffer));

            final float V0 = 1.0f;
            final float V1 = 0.498f;
            final float V2 = 0.153f;

            int renderColorLocation = 0;
            glUniform3f(renderColorLocation, V0, V1, V2);
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

            int VTD = 6; // need to process 6 Vertices To Draw 2 triangles
            final int COUNT = indices.length;
            glDrawElements(GL_TRIANGLES, COUNT, GL_UNSIGNED_INT, 0L);
            glfwSwapBuffers(window);
        }
    } // renderObjects

}