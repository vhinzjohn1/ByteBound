# ByteBound

A small Java adventure. Read the question, then walk into the portal that shows the right answer.

## How to run

Install a Java 11 or newer JDK. The JLayer dependency and game assets are already included.

### Windows

Double-click `run.bat`, or run it from Command Prompt:

```
run.bat
```

### macOS and Linux

```
./run.sh
```

The script compiles into `bin` and starts the game with the assets and bundled JLayer library on the classpath. In VS Code, open this folder and run `src/ByteBound.java`; the project settings include the library.

`bin` contains generated class files; the source assets stay in `src`.

### Run without Java installed (Windows x64)

On a Linux x86_64 build machine with `curl`, `unzip`, `zip`, and a JDK, run:

```
./package-windows.sh
```

Copy `dist/ByteBound-windows-x64.zip` to Windows, extract it, and double-click `run.bat` inside the extracted `ByteBound` folder. The ZIP includes a Java 21 runtime, so Java does not need to be installed on the Windows machine.

### Run without Java installed (Linux x86_64)

On a Linux x86_64 build machine with a JDK that includes `jpackage`, run:

```
./package-linux.sh
```

Copy `dist/ByteBound-linux-x86_64.tar.gz` to the target Linux x86_64 machine, extract it, and launch `ByteBound/bin/ByteBound`. The package contains its own Java runtime, so Java does not need to be installed on the target machine. Native packages are operating-system-specific; Windows and macOS packages must be built on their respective platforms.

## How to play

1. Click **START GAME**, pick Girl or Boy, then **CONFIRM**.
2. Walk with **W A S D**.
3. Read the question at the top of the screen.
4. Three portals on the right each show an answer. Only one is correct. Walk into that portal.
5. A correct portal plays a green animation, gives **10 points**, and loads the next question.
6. A wrong portal plays a red animation and costs **1 heart**. You start with **3 hearts**. The same question stays up so you can try again.
7. Step into the swirling doorway. After the animation, right or wrong, you return to the left and walk over again.
8. Answer all 5 questions to win. Lose every heart and the game ends.
9. On the win or game over panel, choose **PLAY AGAIN** or **MAIN MENU**. **R** also plays again.
10. Background music starts from the beginning and repeats when the track ends.

## Checklist

### Done

- Title screen
- Character select (Girl and Boy)
- Walking and sprites

### Done in this update

- Questions and answers in one file (`src/QuestionBank.java`)
- Three portals for each question, with idle, correct, and wrong animations
- Score and hearts
- Animated idle characters on the selection screen
- Win and game over screens with matching chest/gravestone art, with **R** to retry

### Later

- Sound when an answer is right or wrong
- Arrow keys as well as W A S D
- Shuffle which portal is correct each time you play
- A map that scrolls to the right

## Where to add a question

Open `src/QuestionBank.java` and add one more `new Question(...)`.

Each question needs the prompt, three short answers, and the correct answer's index (`0` top portal, `1` middle, `2` bottom).
