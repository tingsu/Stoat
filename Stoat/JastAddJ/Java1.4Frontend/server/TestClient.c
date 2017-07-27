#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h> 

void error(char *msg)
{
    perror(msg);
    exit(0);
}

int main(int argc, char *argv[])
{
    int sockfd, portno, n, i;
    struct sockaddr_in serv_addr;
    struct hostent *server;

    char buffer[256];
    int length;
    if (argc < 2) {
       fprintf(stderr,"usage %s sourcename\n", argv[0]);
       exit(0);
    }
    portno = 12345;
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) 
        error("ERROR opening socket");
    server = gethostbyname("localhost");
    if (server == NULL) {
        fprintf(stderr,"ERROR, no such host\n");
        exit(0);
    }
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr, 
         (char *)&serv_addr.sin_addr.s_addr,
         server->h_length);
    serv_addr.sin_port = htons(portno);
    if (connect(sockfd,&serv_addr,sizeof(serv_addr)) < 0) 
        error("ERROR connecting");

    getcwd(buffer, 255);
    strncat(buffer, "/\n", 2);
    n = write(sockfd, buffer, strlen(buffer));
    if (n < 0)
         error("ERROR writing to socket");
    
    for(i = 1; i < argc; i++) {
      bzero(buffer,256);
      strncat(buffer, argv[i], strlen(argv[i]));
      strncat(buffer, "\n", 1);
      n = write(sockfd, buffer, strlen(buffer));
      if (n < 0)
           error("ERROR writing to socket");
    }

    n = write(sockfd, "end\n", 4);
    if (n < 0)
         error("ERROR writing to socket");
    
    bzero(buffer,256);
    length = 0;
    n = 1;
    while(n > 0 && length < 255 &&  (length == 0 || buffer[length-1] != '\n')) {
      n = read(sockfd,&buffer[length],255);
      length += n;
    }
    if (n < 0) 
         error("ERROR reading from socket");

    close(sockfd);
    if(strncmp("ok\n", buffer, 3) == 0) {
      printf("ok: %s",buffer);
      return 0;
    }
    else if(strncmp("error", buffer, 5) == 0) {
      fprintf(stderr, "error: %s", buffer);
      return 1;
    }
    else {
      printf("unknown return code: %s\n", buffer);
      return 0;
    }
}

