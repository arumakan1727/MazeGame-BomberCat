MAIN_CLASS    := MapGame
SRC_DIR       := src
OUT_DIR       := out
ASSETS_DIR    := assets
JAVA_SOURCES  := $(wildcard ${SRC_DIR}/*.java)
CLASS_FILES   := $(subst ${SRC_DIR}/,${OUT_DIR}/, $(JAVA_SOURCES:.java=.class))

JFX_LIB                    ?= /usr/share/openjfx/lib
MAP_GAME_JAVA_MODULE_OPTS  ?= --module-path ${JFX_LIB} --add-modules javafx.controls,javafx.fxml,javafx.media
JAVAC_FLAGS                := -Xlint:all -g ${MAP_GAME_JAVA_MODULE_OPTS}


all:	out_dir compile_all


run:	all
	java -classpath ${OUT_DIR} ${MAP_GAME_JAVA_MODULE_OPTS} ${MAIN_CLASS}


compile_all:	${CLASS_FILES}


$(OUT_DIR)/%.class:	${SRC_DIR}/%.java
	javac -classpath ${SRC_DIR} ${JAVAC_FLAGS} $< -d ${OUT_DIR}


out_dir:
	mkdir -p ${OUT_DIR}
	\ls assets/ | xargs -I{} ln -snf ../assets/{} ${OUT_DIR}/{}


clean:
	rm -rf ${OUT_DIR}


.PHONY:	all run compile_all out_dir  clean 
