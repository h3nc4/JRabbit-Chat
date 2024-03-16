javac -cp .:libs/amqp-client-5.20.0.jar:libs/slf4j-api-1.7.36.jar:libs/slf4j-simple-1.7.36.jar -d bin src/**/*.java && \
jar cfm app.jar MANIFEST.MF -C bin/ app -C bin/ services README.md LICENSE libs && \
rm -r bin
