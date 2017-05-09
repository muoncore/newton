package io.muoncore.newton.utils.muon;

import io.muoncore.newton.EnableNewton;
import io.muoncore.newton.eventsource.AggregateConfiguration;
import io.muoncore.newton.eventsource.muon.MuonEventSourceRepository;
import javassist.*;
import javassist.bytecode.ClassFile;
import javassist.bytecode.SignatureAttribute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class EnableNewtonRegistrar implements ImportBeanDefinitionRegistrar {

  @Value("${spring.application.name}")
  private String appName;

  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    try {

      initScan(importingClassMetadata);

      MuonLookupUtils.listAllAggregateRootClass().forEach(s -> {

        String context;
        AggregateConfiguration a = s.getAnnotation(AggregateConfiguration.class);

        if (a != null) {
          context = a.context();
        } else {
          throw new IllegalArgumentException("Currently @AggregateConfiguration(context) is required");
        }

        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(makeRepo(s));
        ConstructorArgumentValues vals = new ConstructorArgumentValues();
        vals.addGenericArgumentValue(s);
        vals.addGenericArgumentValue(context);
        beanDefinition.setConstructorArgumentValues(vals);

        registry.registerBeanDefinition("newtonRepo" + s.getSimpleName(), beanDefinition);
        log.debug("Newton Repository in context {} is registered  {}", context, beanDefinition.getBeanClassName());
      });

    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void initScan(AnnotationMetadata importingClassMetadata) throws ClassNotFoundException {
    AnnotationAttributes attributes =
      AnnotationAttributes.fromMap(
        importingClassMetadata.getAnnotationAttributes
          (EnableNewton.class.getName(), false));

    String[] packages = attributes.getStringArray("value");
    List<String> packs = new ArrayList<>();
    packs.addAll(Arrays.asList(packages));
    packs.add(Class.forName(importingClassMetadata.getClassName()).getPackage().getName());
    log.debug("Initialising Newton by scanning the packages {}", packs);
    MuonLookupUtils.init(packs.toArray(new String[packs.size()]));
  }

  private Class makeRepo(Class param) {
    ClassPool defaultClassPool = ClassPool.getDefault();
    defaultClassPool.appendClassPath(new LoaderClassPath(param.getClassLoader()));
    defaultClassPool.appendClassPath(new LoaderClassPath(MuonEventSourceRepository.class.getClassLoader()));
    defaultClassPool.appendSystemPath();
    try {
      CtClass superInterface = defaultClassPool.getCtClass(MuonEventSourceRepository.class
        .getName());

      String repoName = param.getName() + "Repository";


      try {
        return Class.forName(repoName);
      } catch (ClassNotFoundException e) {
        CtClass repositoryInterface = defaultClassPool.makeClass(repoName, superInterface);
        ClassFile classFile = repositoryInterface.getClassFile();

        String sig = "Ljava/lang/Object;Lio/muoncore/newton/eventsource/muon/MuonEventSourceRepository<L" + getSigName(param) + ";>;";
        SignatureAttribute signatureAttribute = new SignatureAttribute(
          classFile.getConstPool(),
          sig);
        classFile.addAttribute(signatureAttribute);

        return repositoryInterface.toClass();
      }
    } catch (NotFoundException | CannotCompileException e) {
      log.error("Unable to register a newton repository", e);
    }

    return null;
  }

  private String getSigName(Class param) {
    return StringUtils.arrayToDelimitedString(param.getName().split("\\."), "/");
  }
}
