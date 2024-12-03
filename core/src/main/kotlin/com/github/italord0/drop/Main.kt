package com.github.italord0.drop

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.FPSLogger
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.MathUtils.random
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.Timer
import com.badlogic.gdx.utils.viewport.FitViewport

class Main : ApplicationListener {

    private lateinit var backgroundTexture: Texture
    private lateinit var bucketTexture: Texture
    private lateinit var dropTexture: Texture
    private lateinit var dropSound: Sound
    private lateinit var music: Music

    private lateinit var spriteBatch: SpriteBatch
    private lateinit var gameViewport: FitViewport
    private lateinit var uiViewport: FitViewport
    private lateinit var bucketSprite: Sprite
    private lateinit var scoreFont: BitmapFont
    private lateinit var fpsLogger: FPSLogger

    private val drops = mutableListOf<Sprite>()
    private var score = 0

    override fun create() {
        //textures
        backgroundTexture = Texture("background.png")
        bucketTexture = Texture("bucket.png")
        dropTexture = Texture("drop.png")

        //sounds
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"))
        music = Gdx.audio.newMusic(Gdx.files.internal("drop.mp3"))

        //sprites
        bucketSprite = Sprite(bucketTexture)
        bucketSprite.setSize(1f, 1f)

        spriteBatch = SpriteBatch()
        gameViewport = FitViewport(8f, 5f)
        uiViewport = FitViewport(800f, 500f)

        //score font
        scoreFont = BitmapFont()
        scoreFont.color = Color.GOLD
        scoreFont.data.setScale(1f)

        //drop timer spawn
        Timer.schedule(object : Timer.Task() {
            override fun run() {
                spawnDrop()
            }
        }, 0f, 1f)

        //misc
        fpsLogger = FPSLogger()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
        uiViewport.update(width, height, true)
    }

    override fun render() {
        input()
        logic()
        draw()
        fpsLogger.log()
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun dispose() {
        backgroundTexture.dispose()
        bucketTexture.dispose()
        dropTexture.dispose()
        dropSound.dispose()
        music.dispose()
        spriteBatch.dispose()
        scoreFont.dispose()
    }

    private fun input() {

        val speed = 5f * Gdx.graphics.deltaTime

        when {
            Gdx.input.isKeyPressed(Input.Keys.RIGHT) -> {
                bucketSprite.translateX(speed)
            }

            Gdx.input.isKeyPressed(Input.Keys.LEFT) -> {
                bucketSprite.translateX(-speed)
            }
        }
    }

    private fun logic() {
        val worldWidth = gameViewport.worldWidth
        val bucketWidth = bucketSprite.width

        bucketSprite.x = MathUtils.clamp(bucketSprite.x, 0f, worldWidth - bucketWidth)

        val iterator = drops.iterator()
        while (iterator.hasNext()) {
            val drop = iterator.next()
            drop.translateY(-2f * Gdx.graphics.deltaTime)

            if (drop.boundingRectangle.overlaps(bucketSprite.boundingRectangle)) {
                score += 1
                dropSound.play()
                iterator.remove()
            }

            if (drop.y + drop.height < 0) {
                iterator.remove()
            }
        }
    }

    private fun draw() {
        ScreenUtils.clear(Color.BLUE)

        // GAME
        gameViewport.apply()
        spriteBatch.projectionMatrix = gameViewport.camera.combined
        spriteBatch.begin()
        spriteBatch.draw(backgroundTexture, 0f, 0f, gameViewport.worldWidth, gameViewport.worldHeight)
        bucketSprite.draw(spriteBatch)
        drops.forEach { it.draw(spriteBatch) }
        spriteBatch.end()

        // UI
        uiViewport.apply()
        spriteBatch.projectionMatrix = uiViewport.camera.combined
        spriteBatch.begin()
        scoreFont.draw(spriteBatch, "Score: $score", 10f, uiViewport.worldHeight - 10f)
        spriteBatch.end()
    }

    private fun spawnDrop() {
        val dropSprite = Sprite(dropTexture)
        dropSprite.setSize(0.7f, 0.7f)
        dropSprite.setPosition(random(2f, gameViewport.worldWidth - dropSprite.width), gameViewport.worldHeight)
        drops.add(dropSprite)
    }

}
