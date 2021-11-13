package nl.vu.cs.s2group.batterybomber.graphics


import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Triangle(
    top: Triple<Float,Float,Float>,
    bl: Triple<Float,Float,Float>,
    br: Triple<Float,Float,Float>,
    color_top: Color,
    color_bl: Color,
    color_br: Color)
{
    private val triangleCoords = floatArrayOf(     // in counterclockwise order:
        top.first, top.second, top.third,                              // top
        bl.first, bl.second, bl.third,      // bottom left
        br.first, br.second, br.third,   // bottom right
    )
    private val vertexColors = floatArrayOf(
        color_top.R, color_top.G, color_top.B, color_top.alpha,
        color_bl.R, color_bl.G, color_bl.B, color_bl.alpha,
        color_br.R, color_br.G, color_br.B, color_br.alpha,
    )

    private val vertexShaderCode =
        """
        /* This matrix member variable provides a hook to manipulate the coordinates of the objects that use this vertex shader */
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec4 vColor;

        varying vec4 uColor;

        void main() {
            // The matrix must be included as a modifier of gl_Position. Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            gl_Position = uMVPMatrix * vPosition;
            uColor = vColor;
        }
        """.trimIndent()

    private val fragmentShaderCode =
        """
        precision mediump float;
        varying vec4 uColor;
        void main() {
            gl_FragColor = uColor;
        }    
        """.trimIndent()

    private val vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(triangleCoords)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    private val colorBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(vertexColors.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(vertexColors)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    private val mProgram: Int

    init {
        val vertexShader: Int = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        mProgram = GLES20.glCreateProgram() // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader) // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader) // add the fragment shader to program
        GLES20.glLinkProgram(mProgram) // creates OpenGL ES program executables
    }



    //Specify how to draw the triangle
    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0
    private var vPMatrixHandle: Int = 0


    private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition") // get handle to vertex shader's vPosition member
        GLES20.glEnableVertexAttribArray(positionHandle) // Enable a handle to the triangle vertices

        GLES20.glVertexAttribPointer( // Prepare the triangle coordinate data
            positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
            vertexStride, vertexBuffer
        )

        mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor")   // get handle to vertex shader's vColor member
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix") // get handle to shape's transformation matrix
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0) // Pass the projection and view transformation to the shader


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount) // Draw the triangle
        GLES20.glDisableVertexAttribArray(positionHandle) // Disable vertex array
    }

    companion object {
        // number of coordinates per vertex in this array
        private const val COORDS_PER_VERTEX = 3
    }

}
