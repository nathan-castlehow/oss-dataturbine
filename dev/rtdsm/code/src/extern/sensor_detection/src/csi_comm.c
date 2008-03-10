#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h>
#include <stdio.h>
#include <assert.h>
#include <ctype.h>
#include <pthread.h>
#include <string.h>
#include <termios.h>



struct Stdin {
    struct termios org_tios, cur_tios;
} stdin_st;
void Stdin__ctor(struct Stdin *si);
void Stdin__dtor(struct Stdin *si);
void Stdin__cbreak(struct Stdin *si);
void Stdin__cooked(struct Stdin *si);



int serial_fd = -1;
void init_serial(const char *dev_name);



void *reader(void *);



int
main(int argc, char *argv[]) {

    int ec;
    int brk_value; 

    assert(argc == 2);


    // Initialize seriali port.
    init_serial(argv[1]);

    Stdin__ctor(&stdin_st);
    Stdin__cbreak(&stdin_st);

    pthread_t tid;
    ec = pthread_create(&tid, 0, reader, 0);
    assert(ec == 0);

    while (1) {

        int c = getchar();
        //printf("\rread %d\n", c);
        // If control-D, then exit.
        if (c == ('D'^0x40)) {
            break;
        }

        char b = c;
        ec = write(serial_fd, &b, 1);
        assert(ec == 1);
        //printf("\rwrote %d\n", (int) b);
    }

    Stdin__dtor(&stdin_st);
    putchar('\n');

    return 0;
}

void *
reader(void *vp) {
    
    while (1) {

	char buf[80];
	int i, n, write_chr;

	n = read(serial_fd, buf, 80);
        if (n < 0) {
            perror("read");
            assert(0);
        }
	
	for (i = 0; i < n; i++) {
	    if (!isprint(buf[i]) && !isspace(buf[i])) {
		buf[i] = '#';
	    }
	}

	write_chr = write(1, buf, n);
	assert(write_chr == n);
    }
}

void
Stdin__ctor(struct Stdin *si) {

    int ec;

    // Save input tty settings.
    ec = tcgetattr(0, &si->cur_tios);
    assert(ec == 0);

    si->org_tios = si->cur_tios;

    /*
     * Set some things that are always desired.
     */

    // Turn off sending ^S.
    si->cur_tios.c_iflag &= ~IXOFF;
    // Turn on responding to ^S.
    si->cur_tios.c_iflag |= (IXON);
    // Turn off post-processing, like newline conversion.
    // I don't see why we would ever need it. -kec
    si->cur_tios.c_oflag &= ~OPOST;
    // Set byte size.
    si->cur_tios.c_cflag &= ~CSIZE;
    si->cur_tios.c_cflag |= CS8;
    // Set to read single characters.
    si->cur_tios.c_cc[VMIN]=1;
    si->cur_tios.c_cc[VTIME]=0;

    ec = tcsetattr(0, TCSANOW, &si->cur_tios);
    assert(ec == 0);
}

void
Stdin__cbreak(struct Stdin *si) {

    int ec;

    // Don't convert CR to NL.
    si->cur_tios.c_iflag &= ~ICRNL;
    // Turn off local echo.
    si->cur_tios.c_lflag &= ~ECHO;
    // Turn off line-assembly, kill, erase, etc.
    si->cur_tios.c_lflag &= ~ICANON;

    ec = tcsetattr(0, TCSANOW, &si->cur_tios);
    assert(ec == 0);
}

void
Stdin__cooked(struct Stdin *si) {

    int ec;

    // Convert CR to NL.
    si->cur_tios.c_iflag |= ICRNL;
    // Turn on local echo.
    si->cur_tios.c_lflag |= ECHO;
    // Turn on line-assembly, kill, erase, etc.
    si->cur_tios.c_lflag |= ICANON;

    ec = tcsetattr(0, TCSANOW, &si->cur_tios);
    assert(ec == 0);
}

void
Stdin__dtor(struct Stdin *si) {

    int ec;

    ec = tcsetattr(0, TCSANOW, &si->org_tios);
    assert(ec == 0);
}

void
init_serial(const char *dev_name) {

    struct termios tios;
    int ec;

    // If no device is connected, this could block
    // if control lines are not ignored (CLOCAL). -kec
    serial_fd = open(dev_name, O_RDWR|O_NONBLOCK);
    if (serial_fd < 0) {
        perror("open");
        exit(1);
    }

    // Best practice is to read everything from
    // the serial port first, I think, in case
    // there are things like the line discipline
    // that are not well-documented. -kec
    ec = tcgetattr(serial_fd, &tios);
    if (ec < 0) {
        perror("tcgetattr");
        exit(1);
    }

    cfsetispeed(&tios,B9600);
    cfsetospeed(&tios,B9600);

    // Below line was copied from csi2orb.c, I believe. -kec
    // tios.c_iflag &= ~(BRKINT|ICRNL|INPCK|ISTRIP|IXON);
    // Below line is what cfmakeraw will do. -kec
    tios.c_iflag &= ~(IGNBRK|BRKINT|PARMRK|ISTRIP|INLCR|IGNCR|ICRNL|IXON);
    tios.c_oflag &= ~OPOST;
    tios.c_cflag &= ~(CSIZE|PARENB);
    tios.c_cflag |= CS8;
    tios.c_lflag &= ~(ECHO|ECHONL|ICANON|ISIG|IEXTEN);

    tios.c_cc[VMIN]=1;
    tios.c_cc[VTIME]=0;

    ec = tcsetattr(serial_fd, TCSANOW, &tios);
    if (ec != 0) {
        perror("tcsetattr");
        abort();
    }

    // We want blocking behavior now, so turn blocking behavior back on.
    int flags = fcntl(serial_fd, F_GETFL);
    flags &= ~O_NONBLOCK;
    ec = fcntl(serial_fd, F_SETFL, flags);
    assert(ec == 0);
    printf("done setting serial.\n");
}

// vim: set sw=4 sts=4 ai:
