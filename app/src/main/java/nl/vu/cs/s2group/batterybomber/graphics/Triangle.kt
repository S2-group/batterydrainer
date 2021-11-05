package nl.vu.cs.s2group.batterybomber.graphics


import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

// number of coordinates per vertex in this array
private const val COORDS_PER_VERTEX = 3
var triangleCoords = floatArrayOf(     // in counterclockwise order:
    0.0f, 0.622008459f, 0.0f,      // top
    -0.5f, -0.311004243f, 0.0f,    // bottom left
    0.5f, -0.311004243f, 0.0f      // bottom right
)

class Triangle {

    private val vertexShaderCode =
        """
        /* This matrix member variable provides a hook to manipulate the coordinates of the objects that use this vertex shader */
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        void main() {
            // The matrix must be included as a modifier of gl_Position. Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            gl_Position = uMVPMatrix * vPosition;
        }
        """.trimIndent()

    private val fragmentShaderCode =
        """
        precision mediump float;
        uniform vec4 vColor;
        void main() {
            gl_FragColor = vColor;
        }    
        """.trimIndent()

    // Set color with red, green, blue and alpha (opacity) values
    private val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    private var vertexBuffer: FloatBuffer =
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

    private var mProgram: Int

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

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor") // get handle to fragment shader's vColor member
        GLES20.glUniform4fv(mColorHandle, 1, color, 0) // Set color for drawing the triangle

        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix") // get handle to shape's transformation matrix
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0) // Pass the projection and view transformation to the shader


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount) // Draw the triangle
        GLES20.glDisableVertexAttribArray(positionHandle) // Disable vertex array
    }


}
