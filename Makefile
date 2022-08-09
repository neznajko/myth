################################################################
PROJECT := myth
SRCPATH := src/myth
BINPATH := bin
COMPILE := javac -d $(BINPATH) -cp $(BINPATH)

.PHONY: all clean parser myth vm

.DEFAULT_GOAL := all

clean:
	@$(RM) -v $(BINPATH)/$(PROJECT)/*

parser: 
	$(COMPILE) $(SRCPATH)/parser.java

myth: 
	$(COMPILE) $(SRCPATH)/Myth.java

vm:
	$(COMPILE) $(SRCPATH)/vm.java

operator:
	$(COMPILE) $(SRCPATH)/operator.java

all:
	$(COMPILE) $(wildcard $(SRCPATH)/*.java)

################################################################
