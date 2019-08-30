/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.artemis.datastructures.util;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.Security;
import java.util.Arrays;
import java.util.List;
import org.apache.tuweni.bytes.Bytes;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tech.pegasys.artemis.datastructures.operations.DepositData;
import tech.pegasys.artemis.util.bls.BLSKeyPair;
import tech.pegasys.artemis.util.mikuli.KeyPair;
import tech.pegasys.artemis.util.mikuli.SecretKey;

@Disabled
// TODO: Rework expected deposits for new SSZ/SOS scheme.
class MockStartDepositGeneratorTest {

  private static final String[] PRIVATE_KEYS = {
    "0x0000000000000000000000000000000025295F0D1D592A90B333E26E85149708208E9F8E8BC18F6C77BD62F8AD7A6866",
    "0x0000000000000000000000000000000051D0B65185DB6989AB0B560D6DEED19C7EAD0E24B9B6372CBECB1F26BDFAD000",
    "0x00000000000000000000000000000000315ED405FAFE339603932EEBE8DBFD650CE5DAFA561F6928664C75DB85F97857",
    "0x0000000000000000000000000000000025B1166A43C109CB330AF8945D364722757C65ED2BFED5444B5A2F057F82D391",
    "0x000000000000000000000000000000003F5615898238C4C4F906B507EE917E9EA1BB69B93F1DBD11A34D229C3B06784B",
    "0x00000000000000000000000000000000055794614BC85ED5436C1F5CAB586AAB6CA84835788621091F4F3B813761E7A8",
    "0x000000000000000000000000000000001023C68852075965E0F7352DEE3F76A84A83E7582C181C10179936C6D6348893",
    "0x000000000000000000000000000000003A941600DC41E5D20E818473B817A28507C23CDFDB4B659C15461EE5C71E41F5",
    "0x00000000000000000000000000000000066E3BDC0415530E5C7FED6382D5C822C192B620203CF669903E1810A8C67D06",
    "0x000000000000000000000000000000002B3B88A041168A1C4CD04BDD8DE7964FD35238F95442DC678514F9DADB81EC34"
  };

  private static final String[] EXPECTED_DEPOSITS = {
    "0x30000000A99A76ED7796F7BE22D5B7E85DEEB7C5677E88E511E0B337618F8C4EB61349B4BF2D153F649F7B53359FE8B94A38E44C00FAD2A6BFB0E7F1F0F45460944FBD8DFA7F37DA06A4D13B3983CC90BB46963B004059730700000060000000B2138023C71BFDD717F73992668876479534F2439CE7D8AA0A991E39275B8DF4548CCDBF653566D912347CA60665F887066466317B234DC632F265EE337B18BCEAE9B1C5C49A896F560B5FB82E7E296E2968C313BF9C9C7391A68892E4CA1CD5",
    "0x30000000B89BEBC699769726A318C8E9971BD3171297C61AEA4A6578A7A4F94B547DCBA5BAC16A89108B6B6A1FE3695D1A874A0B00EC7EF7780C9D151597924036262DD28DC60E1228F4DA6FECF9D402CB3F35940040597307000000600000009208CBA46071DEC19DBB965A086E041079296018A5DDB796AAF78BB932A89E40C532941EAF96204FF123B69A96968FB71829EB26544E4B04CCF8FDE6F478DF101052FCE1473AC8C190E10EEFE9EF97EDB10C09CE3FF55DA92733A663A714B560",
    "0x30000000A3A32B0F8B4DDB83F1A0A853D81DD725DFE577D4F4C3DB8ECE52CE2B026ECA84815C1A7E8E92A4DE3D755733BF7E4A9B0036085C6C608E6D048505B04402568C36CCE1E025722DE44F9C3685A5C80FA6004059730700000060000000B52DBAE54D4BAF54CE3082964439E13A2774BED9A3C4307C425CC67B1F7DDAF6484187649AEA77BC506509E7F001F9E70495D0F2C899CC90E29C252EF6E3EA4542293E0F53E66C4C9A5402E92A4F91FD7D5039BCB7264EB6C2E5638BEA14E9D8",
    "0x3000000088C141DF77CD9D8D7A71A75C826C41A9C9F03C6EE1B180F3E7852F6A280099DED351B58D66E653AF8E42816A4D8F532E005A7DE495BCEC04D3B5E74AE09FFE493A9DD06D7DCBF18C78455571E87D901A004059730700000060000000B0B301A1D7E0AAD88C3D12168C0D52815EACC22AB9A24DBD8E9B770FC25D40319F5A2584F21E9AF7F18F9C5289CCCA1019A2E3ADD634D898B90429D6043D17A0D7B6E7D260271FBE65F754D5653C60C233DDB6CE516C4C101EE5A42ACE0572A3",
    "0x3000000081283B7A20E1CA460EBD9BBD77005D557370CABB1F9A44F530C4C4C66230F675F8DF8B4C2818851AA7D77A80CA5A4A5E004A28C193C65C91B7EBB5B5D14FFA7F75DC48AD4BC66DE82F70FC55A2DF1215004059730700000060000000A20A2A0517F1BFE9828E0801E780968A5A6A797193E5437063586FF536984B86C74039A99FEB81DAB9EA83E14926E6EC050E3805A0E2648016EC229FF3694E3B7DE711015370569772CFE5913B1751326F9CC92DB94781A84BCA63CB5037B20F",
    "0x30000000AB0BDDA0F85F842F431BEACCF1250BF1FD7BA51B4100FD64364B6401FDA85BB0069B3E715B58819684E7FC0B10A72A34005856AB195B61DF2FF5D6AB2FA36F30DAB45E42CFA1AAEF3FFD899F29BD8641004059730700000060000000A3AD6DA5207A80A36930D928235B3EE3AECC5DF5440F05B989EBB3EF2A18B4151A359321D83AE4B358A7673C32D8B3BC133CE682552FD8F80E41F3FABFD5B1C02CF9BE2C071D62257D6324B1FB15916C0DEA12B16CCA97138537D8EE7A430666",
    "0x300000009977F1C8B731A8D5558146BFB86CAEA26434F3C5878B589BF280A42C9159E700E9DF0E4086296C20B011D2E78C27D373001C5D9BEDBAD1B7AFF3B80E887E65B3357A695B70B6EE0625C2B2F6F86449F800405973070000006000000099CB0D6C7DD9C10D4E742FC4A8C24146ACA412680FB1623C379B92AC9946FA9864F23A04ED38B0CDF795B8F218D1FB9602BB15A9E0557BAC30ECEF28E863CD9495FE397967C87FBA6463908C5C1ED085D136B16A816A5EAE4AA1E64A9961202D",
    "0x30000000A8D4C7C27795A725961317EF5953A7032ED6D83739DB8B0E8A72353D1B8B4439427F7EFA2C89CAA03CC9F28F8CBAB8AC001414BFC6DACCA55F974EC910893C8617F9C99DA897534C637B50E9FC69532300405973070000006000000087725D25C9A5C2B7B3304B509541D875B8AD24F1277ABE7557BE6CDB24A7D66410A8AE7E4A816F4C45FBE9C7C3059AF1059AA6C07674803DDC3C2C7E27B9ED3CA6361B0221947636E9B59FAEB5ADB4500D47F6D03FCEADB119B8EAD125AD52FE",
    "0x30000000A6D310DBBFAB9A22450F59993F87A4CE5DB6223F3B5F1F30D2C4EC718922D400E0B3C7741DE8E59960F72411A0EE10A700ED09B6181E6F97365E221E70AEEBCB2604011D8C4326F3B98CE8D79B031AE8004059730700000060000000AA5D3E1C0868D2BAA139C4AAB1299814AE5E44D55FD4359D8DEC1C981602FFD351B5A143C16F5E6298BC2F8A2B5B97A715EAC0A52FF33FDCCB8FDB048E7A0B8D71F72297F24159380F6E3572C59ABC3E1A6BB737EE25C73E4C7D1F910BAD4CD1",
    "0x300000009893413C00283A3F9ED9FD9845DDA1CEA38228D22567F9541DCCC357E54A2D6A6E204103C92564CBC05F4905AC7C493A001FE05BAA70DD29CE85F694898BB6DE3BCDE158A825DB56906B54141B2A728D0040597307000000600000009706E68564B373EFD0A2DB8FD089DD77B5092CCDF94D3AF0D85EAD1C6757784979E9C23E427A597594490328AC54B81616BAF9306F6AD4158671BA3FA812DD8381259C4BFE4004450A0F5D6C7D69A39EE844D9EA84FD31FBE21B6AF6F50BB4D3",
  };

  private final MockStartDepositGenerator generator = new MockStartDepositGenerator();

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  @Test
  public void shouldGenerateDepositData() {
    final List<BLSKeyPair> keyPairs =
        Arrays.stream(PRIVATE_KEYS)
            .map(Bytes::fromHexString)
            .map(SecretKey::fromBytes)
            .map(KeyPair::new)
            .map(BLSKeyPair::new)
            .collect(toList());

    final List<DepositData> expectedDeposits =
        Arrays.stream(EXPECTED_DEPOSITS)
            .map(Bytes::fromHexString)
            .map(DepositData::fromBytes)
            .collect(toList());

    final List<DepositData> actualDeposits = generator.createDeposits(keyPairs);
    assertEquals(expectedDeposits, actualDeposits);
  }
}
